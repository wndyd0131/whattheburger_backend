import { goldenLine, buildDefaultOption } from './templates.js';
import { chance, weightedPick1 } from './random.js';

function productNameKey(productDetail) {
  return productDetail.productName;
}

function getRuleWeights(weights, productName, ruleName) {
  const table = weights.byProductName;
  if (!table || !productName) {
    return null;
  }
  return table[productName]?.[ruleName] ?? null;
}

function getModifyProbability(weights, productName, ruleName) {
  const ruleWeights = getRuleWeights(weights, productName, ruleName);
  if (ruleWeights != null && ruleWeights.modify != null) {
    return ruleWeights.modify;
  }
  return weights.modifyRule?.[ruleName] ?? 0;
}

function equalWeightsForOptions(options) {
  const map = {};
  for (const o of options) {
    map[o.productOptionId] = 1;
  }
  return map;
}

function groupRuleBlocks(productDetail) {
  const byRuleId = {};
  for (const option of productDetail.optionResponses || []) {
    const rule = option.customRuleResponse;
    if (!rule || rule.customRuleId == null) {
      continue;
    }
    const ruleId = rule.customRuleId;
    if (!byRuleId[ruleId]) {
      byRuleId[ruleId] = { rule, options: [] };
    }
    byRuleId[ruleId].options.push(option);
  }
  const blocks = Object.values(byRuleId);
  for (const block of blocks) {
    block.options.sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0));
  }
  blocks.sort(
    (a, b) => (a.rule.orderIndex ?? 0) - (b.rule.orderIndex ?? 0)
  );
  return blocks;
}

function bernoulliP(entry) {
  if (entry != null && typeof entry === 'object' && entry.p != null) {
    return Number(entry.p);
  }
  return 0;
}

function uniqueWeightMap(ruleWeights, options) {
  const fromWeights = ruleWeights?.options;
  if (fromWeights && Object.keys(fromWeights).length > 0) {
    const map = {};
    for (const [id, w] of Object.entries(fromWeights)) {
      const n = Number(w);
      if (Number.isFinite(n) && n > 0 && typeof w !== 'object') {
        map[id] = n;
      }
    }
    if (Object.keys(map).length > 0) {
      return map;
    }
  }
  return equalWeightsForOptions(options);
}

function selectUnique(block, ruleWeights) {
  const weightMap = uniqueWeightMap(ruleWeights, block.options);
  const pickedId = weightedPick1(weightMap);
  const picked = block.options.find((o) => o.productOptionId === pickedId);
  return picked ? [picked] : [];
}

function selectFree(block, ruleWeights) {
  const selected = [];
  const weightTable = ruleWeights?.options ?? {};
  for (const option of block.options) {
    const id = option.productOptionId;
    const entry = weightTable[id] ?? weightTable[String(id)];
    const p = bernoulliP(entry);
    if (chance(p)) {
      selected.push(option);
    }
  }
  return selected;
}

function shuffleInPlace(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
}

function selectLimit(block, ruleWeights) {
  const rule = block.rule;
  const min = rule.minSelection ?? 0;
  const max = rule.maxSelection ?? block.options.length;
  let selected = selectFree(block, ruleWeights);

  if (selected.length > max) {
    shuffleInPlace(selected);
    selected = selected.slice(0, max);
  }

  if (selected.length < min) {
    const selectedIds = new Set(selected.map((o) => o.productOptionId));
    const pool = block.options.filter((o) => !selectedIds.has(o.productOptionId));
    shuffleInPlace(pool);
    while (selected.length < min && pool.length > 0) {
      selected.push(pool.pop());
    }
  }

  return selected;
}

function selectOptions(block, ruleWeights) {
  const type = block.rule.customRuleType;
  if (type === 'UNIQUE') {
    return selectUnique(block, ruleWeights);
  }
  if (type === 'FREE') {
    return selectFree(block, ruleWeights);
  }
  if (type === 'LIMIT') {
    return selectLimit(block, ruleWeights);
  }
  return [];
}

function buildEligiblePooqWeights(option, weights) {
  const details = option.quantityDetailResponses || [];
  const available = new Set(details.map((d) => d.quantityType));
  const tables = [weights.pooq ?? {}, weights.pooqSizes ?? {}];
  const eligible = {};
  for (const table of tables) {
    for (const [type, w] of Object.entries(table)) {
      if (!available.has(type)) {
        continue;
      }
      const n = Number(w);
      if (Number.isFinite(n) && n > 0) {
        eligible[type] = n;
      }
    }
  }
  if (Object.keys(eligible).length === 0) {
    for (const d of details) {
      if (d.quantityType) {
        eligible[d.quantityType] = 1;
      }
    }
  }
  return eligible;
}

function pickPooqDetail(option, weights) {
  const details = option.quantityDetailResponses || [];
  if (details.length === 0) {
    return null;
  }
  const eligible = buildEligiblePooqWeights(option, weights);
  const pickedType = weightedPick1(eligible);
  if (pickedType != null) {
    const match = details.find((d) => d.quantityType === pickedType);
    if (match) {
      return match;
    }
  }
  return details.find((d) => d.isDefault === true) ?? details[0];
}

function buildOptionPayload(option, weights, { randomPooq = false } = {}) {
  if (!randomPooq || option.countType !== 'UNCOUNTABLE') {
    return buildDefaultOption(option);
  }

  const payload = {
    productOptionId: option.productOptionId,
    countType: option.countType,
    quantity: 1,
  };
  const pooq = pickPooqDetail(option, weights);
  if (pooq) {
    payload.quantityDetail = {
      productOptionOptionQuantityId: pooq.id,
      quantityType: pooq.quantityType,
    };
  }
  return payload;
}

function goldenRuleById(line) {
  const map = {};
  for (const rulePayload of line.orderCustomRules || []) {
    map[rulePayload.customRuleId] = rulePayload;
  }
  return map;
}

function applyRandomRules(line, productDetail, weights) {
  const productName = productNameKey(productDetail);
  const blocks = groupRuleBlocks(productDetail);
  const goldenByRuleId = goldenRuleById(line);
  const resultByRuleId = {};

  for (const block of blocks) {
    const ruleId = block.rule.customRuleId;
    const ruleName = block.rule.name;
    const modifyP = getModifyProbability(weights, productName, ruleName);

    if (!chance(modifyP)) {
      const goldenRule = goldenByRuleId[ruleId];
      if (goldenRule && (goldenRule.orderProductOptions || []).length > 0) {
        resultByRuleId[ruleId] = goldenRule;
      }
      continue;
    }

    const ruleWeights = getRuleWeights(weights, productName, ruleName);
    const selected = selectOptions(block, ruleWeights);
    if (selected.length === 0) {
      continue;
    }

    resultByRuleId[ruleId] = {
      customRuleId: ruleId,
      orderProductOptions: selected.map((o) =>
        buildOptionPayload(o, weights, { randomPooq: true })
      ),
    };
  }

  const orderCustomRules = blocks
    .map((block) => resultByRuleId[block.rule.customRuleId])
    .filter(
      (payload) =>
        payload && (payload.orderProductOptions || []).length > 0
    );

  return { ...line, orderCustomRules };
}

export function genOrder(productDetail, weights) {
  const line = goldenLine(productDetail);
  const updated = applyRandomRules(line, productDetail, weights);
  return { orderProducts: [updated] };
}

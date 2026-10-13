function buildOptionIndex(productDetail) {
  const optionByProductOptionId = {};
  for (const option of productDetail.optionResponses || []) {
    optionByProductOptionId[option.productOptionId] = option;
  }
  return optionByProductOptionId;
}

function ruleExistsInSnapshot(customRuleId, productDetail) {
  return (productDetail.optionResponses || []).some(
    (o) => o.customRuleResponse?.customRuleId === customRuleId
  );
}

function validateOptionPayload(optionPayload, snapshotOption, ruleId) {
  const errors = [];
  const id = optionPayload.productOptionId;

  if (!snapshotOption) {
    errors.push(`productOptionId ${id} not in snapshot`);
    return errors;
  }

  const rule = snapshotOption.customRuleResponse;
  if (!rule || rule.customRuleId !== ruleId) {
    errors.push(`productOptionId ${id} not in customRuleId ${ruleId}`);
  }

  if (optionPayload.countType !== snapshotOption.countType) {
    errors.push(
      `productOptionId ${id}: countType ${optionPayload.countType} != snapshot ${snapshotOption.countType}`
    );
  }

  const countType = snapshotOption.countType;

  if (countType === 'NONE') {
    return errors;
  }

  if (countType === 'COUNTABLE') {
    const q = optionPayload.quantity;
    if (q == null || !Number.isInteger(q) || q < 1) {
      errors.push(`productOptionId ${id}: COUNTABLE needs quantity >= 1`);
    } else if (
      snapshotOption.maxQuantity != null &&
      q > snapshotOption.maxQuantity
    ) {
      errors.push(
        `productOptionId ${id}: quantity ${q} > maxQuantity ${snapshotOption.maxQuantity}`
      );
    }
    return errors;
  }

  if (countType === 'UNCOUNTABLE') {
    const q = optionPayload.quantity;
    if (q == null || !Number.isInteger(q) || q < 1) {
      errors.push(`productOptionId ${id}: UNCOUNTABLE needs quantity >= 1`);
    }
    const pooqId = optionPayload.quantityDetail?.productOptionOptionQuantityId;
    if (pooqId == null) {
      errors.push(`productOptionId ${id}: UNCOUNTABLE needs quantityDetail POOQ id`);
      return errors;
    }
    const allowed = (snapshotOption.quantityDetailResponses || []).map(
      (d) => d.id
    );
    if (!allowed.includes(pooqId)) {
      errors.push(
        `productOptionId ${id}: POOQ id ${pooqId} not in snapshot quantityDetailResponses`
      );
    }
    return errors;
  }

  return errors;
}

function validateRuleSelection(rulePayload, snapshotRuleMeta, ruleLabel) {
  const errors = [];
  const options = rulePayload.orderProductOptions || [];
  const selectedCount = options.length;
  const type = snapshotRuleMeta.customRuleType;

  if (type === 'UNIQUE') {
    if (selectedCount !== 1) {
      errors.push(
        `${ruleLabel}: UNIQUE requires 1 option, got ${selectedCount}`
      );
    }
    return errors;
  }

  if (type === 'FREE' || type === 'LIMIT') {
    const min = snapshotRuleMeta.minSelection ?? 0;
    const max = snapshotRuleMeta.maxSelection ?? selectedCount;
    if (selectedCount < min) {
      errors.push(
        `${ruleLabel}: selected ${selectedCount} < minSelection ${min}`
      );
    }
    if (selectedCount > max) {
      errors.push(
        `${ruleLabel}: selected ${selectedCount} > maxSelection ${max}`
      );
    }
  }

  return errors;
}

export function validateOrderProductLine(line, productDetail) {
  const errors = [];

  if (line.storeProductId !== productDetail.storeProductId) {
    errors.push(
      `storeProductId ${line.storeProductId} != snapshot ${productDetail.storeProductId}`
    );
  }

  const qty = line.quantity;
  if (qty == null || !Number.isInteger(qty) || qty < 1) {
    errors.push('line quantity must be integer >= 1');
  }

  const optionIndex = buildOptionIndex(productDetail);
  const rules = line.orderCustomRules || [];

  for (const rulePayload of rules) {
    const ruleId = rulePayload.customRuleId;
    const ruleLabel = `customRuleId ${ruleId}`;

    if (!ruleExistsInSnapshot(ruleId, productDetail)) {
      errors.push(`${ruleLabel} not in product snapshot`);
      continue;
    }

    const optionsInRule = rulePayload.orderProductOptions || [];
    let snapshotRuleMeta = null;

    for (const optionPayload of optionsInRule) {
      const snapshotOption = optionIndex[optionPayload.productOptionId];
      if (snapshotOption?.customRuleResponse && !snapshotRuleMeta) {
        snapshotRuleMeta = snapshotOption.customRuleResponse;
      }
      errors.push(
        ...validateOptionPayload(optionPayload, snapshotOption, ruleId)
      );
    }

    if (snapshotRuleMeta) {
      errors.push(
        ...validateRuleSelection(rulePayload, snapshotRuleMeta, ruleLabel)
      );
    }
  }

  return errors;
}

function getProductDetail(products, storeProductId) {
  return products[storeProductId] ?? products[String(storeProductId)] ?? null;
}

export function validateDeductPayload(payload, products) {
  const errors = [];
  const lines = payload?.orderProducts;

  if (!Array.isArray(lines) || lines.length === 0) {
    return { valid: false, errors: ['orderProducts must be non-empty array'] };
  }

  lines.forEach((line, index) => {
    const detail = getProductDetail(products, line.storeProductId);
    if (!detail) {
      errors.push(
        `line ${index}: no snapshot for storeProductId ${line.storeProductId}`
      );
      return;
    }
    const lineErrors = validateOrderProductLine(line, detail);
    for (const msg of lineErrors) {
      errors.push(`line ${index}: ${msg}`);
    }
  });

  return { valid: errors.length === 0, errors };
}

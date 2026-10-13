export function buildDefaultOption(option) {
  const payload = {
    productOptionId: option.productOptionId,
    countType: option.countType,
  };

  if (option.countType === 'NONE') {
    return payload;
  }

  if (option.countType === 'COUNTABLE') {
    payload.quantity = option.defaultQuantity ?? 1;
    return payload;
  }

  if (option.countType === 'UNCOUNTABLE') {
    payload.quantity = 1;
    const details = option.quantityDetailResponses || [];
    const defaultPooq = details.find((d) => d.isDefault === true);
    if (defaultPooq) {
      payload.quantityDetail = {
        productOptionOptionQuantityId: defaultPooq.id,
        quantityType: defaultPooq.quantityType,
      };
    }
    return payload;
  }

  return payload;
}

export function goldenLine(productDetail, { quantity = 1 } = {}) {
  const defaults = (productDetail.optionResponses || []).filter(
    (o) => o.isDefault === true
  );

  const byRuleId = {};
  for (const option of defaults) {
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

  const ruleGroups = Object.values(byRuleId);
  ruleGroups.sort(
    (a, b) => (a.rule.orderIndex ?? 0) - (b.rule.orderIndex ?? 0)
  );

  const orderCustomRules = ruleGroups.map(({ rule, options }) => {
    options.sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0));
    return {
      customRuleId: rule.customRuleId,
      orderProductOptions: options.map(buildDefaultOption),
    };
  });

  return {
    storeProductId: productDetail.storeProductId,
    quantity,
    orderCustomRules,
  };
}

export function goldenOrder(productDetail, opts = {}) {
  return {
    orderProducts: [goldenLine(productDetail, opts)],
  };
}

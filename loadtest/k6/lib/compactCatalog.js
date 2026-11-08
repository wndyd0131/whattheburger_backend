/**
 * Raw snapshot { storeIds, byStore, products } -> compact per-VU catalog:
 *   detailsByName: { [productName]: slim detail without storeProductId }
 *   storeProductIdByStore: { [storeId]: { [productName]: storeProductId } }
 * Option data must be identical across stores for the same product name; throws otherwise.
 */

function slimRule(rule) {
  if (!rule) {
    return null;
  }
  return {
    customRuleId: rule.customRuleId,
    customRuleType: rule.customRuleType,
    name: rule.name,
    minSelection: rule.minSelection,
    maxSelection: rule.maxSelection,
    orderIndex: rule.orderIndex,
  };
}

function slimOption(option) {
  return {
    productOptionId: option.productOptionId,
    countType: option.countType,
    isDefault: option.isDefault,
    defaultQuantity: option.defaultQuantity,
    maxQuantity: option.maxQuantity,
    orderIndex: option.orderIndex,
    customRuleResponse: slimRule(option.customRuleResponse),
    quantityDetailResponses: (option.quantityDetailResponses || [])
      .map((d) => ({ id: d.id, quantityType: d.quantityType, isDefault: d.isDefault }))
      .sort((a, b) => a.id - b.id),
  };
}

function slimDetail(detail) {
  return {
    productName: detail.productName,
    optionResponses: (detail.optionResponses || [])
      .map(slimOption)
      .sort((a, b) => a.productOptionId - b.productOptionId),
  };
}

export function compactCatalog(raw) {
  const detailsByName = {};
  const fingerprintByName = {};
  const storeProductIdByStore = {};

  for (const [storeKey, storeSnap] of Object.entries(raw?.byStore ?? {})) {
    const products = storeSnap?.products ?? {};
    const idByName = {};

    for (const [name, id] of Object.entries(storeSnap?.storeProductIdByName ?? {})) {
      const detail = products[id] ?? products[String(id)];
      if (!detail) {
        continue;
      }
      const slim = slimDetail(detail);
      const fingerprint = JSON.stringify(slim);
      if (fingerprintByName[name] == null) {
        fingerprintByName[name] = fingerprint;
        detailsByName[name] = slim;
      } else if (fingerprintByName[name] !== fingerprint) {
        throw new Error(
          `[catalog] option data for "${name}" differs at storeId=${storeKey}; compact catalog assumes identical options across stores`
        );
      }
      idByName[name] = id;
    }

    storeProductIdByStore[storeKey] = idByName;
  }

  return { detailsByName, storeProductIdByStore };
}

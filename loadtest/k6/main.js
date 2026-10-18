/**
 * Loadtest deduct: multi-store setup; each iteration picks store, lineCount, SKUs, golden vs generator.
 * Run from repo root: k6 run loadtest/k6/main.js
 */
import http from 'k6/http';
import { check } from 'k6';
import { goldenLine } from './lib/templates.js';
import { genOrder } from './lib/generator.js';
import { weights } from './lib/weights.js';
import { validateDeductPayload } from './lib/validate.js';
import { chance, weightedPick1 } from './lib/random.js';

export { setup } from './setup.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

function getProductDetail(products, storeProductId) {
  return products[storeProductId] ?? products[String(storeProductId)] ?? null;
}

function fixedProductNameFromEnv() {
  const raw = __ENV.LOADTEST_PRIMARY_PRODUCT_NAME;
  if (raw == null || String(raw).trim() === '') {
    return null;
  }
  return String(raw).trim();
}

function buildEligibleProductWeights(storeProductIdByName, productsByName) {
  const eligible = {};
  const mix = productsByName ?? {};
  for (const name of Object.keys(mix)) {
    if (storeProductIdByName[name] != null) {
      eligible[name] = mix[name];
    }
  }
  return eligible;
}

function pickProductDetail(storeSnapshot, weightConfig) {
  const products = storeSnapshot?.products || {};
  const byName = storeSnapshot?.storeProductIdByName || {};
  const productsByName = weightConfig.productsByName ?? {};

  let productName = fixedProductNameFromEnv();
  if (productName == null) {
    const eligible = buildEligibleProductWeights(byName, productsByName);
    productName = weightedPick1(eligible);
    if (productName == null || typeof productName !== 'string') {
      return null;
    }
  }

  const storeProductId = byName[productName];
  if (storeProductId == null) {
    return null;
  }
  const detail = getProductDetail(products, storeProductId);
  if (!detail) {
    return null;
  }
  return { detail, productName, storeProductId };
}

function pickStoreId(data, weightConfig) {
  const stores = weightConfig.stores ?? {};
  const picked = weightedPick1(stores);
  if (picked == null) {
    return null;
  }
  const storeId = Number(picked);
  if (!Number.isFinite(storeId) || storeId <= 0) {
    return null;
  }
  if (!data?.byStore?.[storeId] && !data?.byStore?.[String(storeId)]) {
    return null;
  }
  return storeId;
}

function getStoreSnapshot(data, storeId) {
  return data.byStore[storeId] ?? data.byStore[String(storeId)] ?? null;
}

function pickLineCount(weightConfig) {
  const table = weightConfig.order?.lineCount ?? { 1: 1 };
  const picked = weightedPick1(table);
  const n = Number(picked);
  if (!Number.isFinite(n) || n < 1) {
    return 1;
  }
  return Math.floor(n);
}

function clamp01(n) {
  if (n < 0) {
    return 0;
  }
  if (n > 1) {
    return 1;
  }
  return n;
}

function resolveGoldenRatio(weightConfig) {
  const fallback = weightConfig.goldenRatio ?? 0.7;
  const raw = __ENV.LOADTEST_GOLDEN_RATIO;
  if (raw == null || String(raw).trim() === '') {
    return clamp01(Number(fallback));
  }
  const parsed = Number(String(raw).trim());
  if (!Number.isFinite(parsed)) {
    return clamp01(Number(fallback));
  }
  return clamp01(parsed);
}

function buildOrderLine(productDetail, useGolden, weightConfig) {
  if (useGolden) {
    return goldenLine(productDetail);
  }
  return genOrder(productDetail, weightConfig).orderProducts[0];
}

function buildDeductPayload(storeSnapshot, weightConfig) {
  const lineCount = pickLineCount(weightConfig);
  const useGolden = chance(resolveGoldenRatio(weightConfig));
  const lines = [];

  for (let i = 0; i < lineCount; i++) {
    const resolved = pickProductDetail(storeSnapshot, weightConfig);
    if (!resolved?.detail) {
      return null;
    }
    lines.push(buildOrderLine(resolved.detail, useGolden, weightConfig));
  }

  return { orderProducts: lines };
}

export const options = {
  vus: 1,
  iterations: 1,
};

export default function (data) {
  const products = data?.products || {};
  const storeId = pickStoreId(data, weights);
  const storeSnapshot = storeId != null ? getStoreSnapshot(data, storeId) : null;

  check(null, {
    'setup loaded products': () => Object.keys(products).length > 0,
    'picked store in snapshot': () => storeSnapshot != null,
  });

  if (!storeSnapshot) {
    console.error('[main] store not found (weights.stores vs setup byStore)');
    return;
  }

  const payload = buildDeductPayload(storeSnapshot, weights);

  check(null, {
    'picked SKU in snapshot': () =>
      payload != null && (payload.orderProducts || []).length > 0,
  });

  if (!payload) {
    const fixed = fixedProductNameFromEnv();
    if (fixed) {
      console.error(
        `[main] product not found for LOADTEST_PRIMARY_PRODUCT_NAME=${fixed}`
      );
    } else {
      console.error(
        '[main] no eligible SKU (weights.productsByName vs store snapshot)'
      );
    }
    return;
  }

  const validation = validateDeductPayload(payload, products);
  check(null, {
    'deduct payload valid': () => validation.valid,
  });
  if (!validation.valid) {
    console.error(validation.errors.join('; '));
    return;
  }

  const url = `${BASE_URL}/api/v1/loadtest/store/${storeId}/inventory/deduct`;
  const res = http.post(url, JSON.stringify(payload), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'POST loadtest inventory deduct' },
  });

  let body;
  try {
    body = res.json();
  } catch (_) {
    body = null;
  }

  check(res, {
    'deduct status is 200': (r) => r.status === 200,
    'deduct body status is DEDUCTED': () => body && body.status === 'DEDUCTED',
  });
}

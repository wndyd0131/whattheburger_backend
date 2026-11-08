/**
 * Loadtest deduct: multi-store catalog; picks store, lineCount, SKUs, golden vs generator.
 * Default: setup() HTTP catalog (BASE_URL). Optional file: export JSON + LOADTEST_CATALOG_SOURCE=file.
 */
import http from 'k6/http';
import { check } from 'k6';
import { goldenLine } from './lib/templates.js';
import { genOrder } from './lib/generator.js';
import { weights } from './lib/weights.js';
import { validateDeductPayload } from './lib/validate.js';
import { chance, weightedPick1 } from './lib/random.js';
import {
  deductRequestHeaders,
  logDeductFailure,
  newDeductTraceId,
} from './lib/httpTrace.js';
import { usesFileCatalog, usesFixturePayload } from './lib/catalogSource.js';
import { getCatalogSnapshot } from './lib/sharedCatalogSnapshot.js';
import { openGoldenFixture } from './lib/openFixture.js';

export { setup } from './setup.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const FIXTURE_BODY = usesFixturePayload() ? openGoldenFixture() : null;

function resolveCatalog(setupData) {
  if (usesFileCatalog()) {
    return getCatalogSnapshot();
  }
  return setupData;
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

function pickProductDetail(catalog, storeIdByName, weightConfig) {
  const byName = storeIdByName || {};
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
  const shared = catalog.detailsByName?.[productName];
  if (!shared) {
    return null;
  }
  const detail = { ...shared, storeProductId };
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
  if (getStoreProductIds(data, storeId) == null) {
    return null;
  }
  return storeId;
}

function getStoreProductIds(data, storeId) {
  const byStore = data?.storeProductIdByStore;
  return byStore?.[storeId] ?? byStore?.[String(storeId)] ?? null;
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

function buildDeductPayload(catalog, storeIdByName, weightConfig) {
  const lineCount = pickLineCount(weightConfig);
  const useGolden = chance(resolveGoldenRatio(weightConfig));
  const lines = [];
  const products = {};

  for (let i = 0; i < lineCount; i++) {
    const resolved = pickProductDetail(catalog, storeIdByName, weightConfig);
    if (!resolved?.detail) {
      return null;
    }
    products[resolved.storeProductId] = resolved.detail;
    lines.push(buildOrderLine(resolved.detail, useGolden, weightConfig));
  }

  return { payload: { orderProducts: lines }, products };
}

const rampOptions = {
  scenarios: {
    deduct_ramp: {
      executor: 'ramping-arrival-rate',
      startRate: 0,
      timeUnit: '1s',
      preAllocatedVUs: 100,
      maxVUs: 500,
      stages: [
        { duration: '1m', target: 10 },
        { duration: '2m', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '2m', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '2m', target: 100 },
      ],
    }
  }
};

export const options = FIXTURE_BODY
  ? { vus: 1, iterations: Number(__ENV.LOADTEST_FIXTURE_ITERATIONS || 1) }
  : rampOptions;

export default function (data) {
  if (FIXTURE_BODY) {
    postDeduct(Number(__ENV.LOADTEST_FIXTURE_STORE_ID || 1), FIXTURE_BODY);
    return;
  }

  const catalog = resolveCatalog(data);
  const storeId = pickStoreId(catalog, weights);
  const storeIdByName = storeId != null ? getStoreProductIds(catalog, storeId) : null;

  check(null, {
    'snapshot loaded products': () =>
      Object.keys(catalog?.detailsByName ?? {}).length > 0,
    'picked store in snapshot': () => storeIdByName != null,
  });

  if (!storeIdByName) {
    console.error('[main] store not found (weights.stores vs setup byStore)');
    return;
  }

  const built = buildDeductPayload(catalog, storeIdByName, weights);
  const payload = built?.payload ?? null;

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

  const validation = validateDeductPayload(payload, built.products);
  check(null, {
    'deduct payload valid': () => validation.valid,
  });
  if (!validation.valid) {
    console.error(validation.errors.join('; '));
    return;
  }

  postDeduct(storeId, JSON.stringify(payload));
}

function postDeduct(storeId, bodyString) {
  const url = `${BASE_URL}/api/v1/loadtest/store/${storeId}/inventory/deduct`;
  const traceId = newDeductTraceId();
  const res = http.post(url, bodyString, {
    headers: deductRequestHeaders(traceId),
    tags: { name: 'POST loadtest inventory deduct' },
  });

  let body;
  try {
    body = res.json();
  } catch (_) {
    body = null;
  }

  const deductOk = res.status === 200 && body && body.status === 'DEDUCTED';

  check(res, {
    'deduct status is 200': (r) => r.status === 200,
    'deduct body status is DEDUCTED': () => body && body.status === 'DEDUCTED',
  });

  if (!deductOk) {
    logDeductFailure({ traceId, storeId, url, res, body });
  }
}

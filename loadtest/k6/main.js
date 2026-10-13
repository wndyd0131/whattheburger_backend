/**
 * Smoke: setup loads menu; each iteration POSTs default (golden) order from templates.
 * Run from repo root: k6 run loadtest/k6/main.js
 */
import http from 'k6/http';
import { check } from 'k6';
import { goldenOrder } from './lib/templates.js';
import { validateDeductPayload } from './lib/validate.js';

export { setup } from './setup.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const STORE_ID = __ENV.STORE_ID || '1';
const PRIMARY_STORE_PRODUCT_ID = Number(
  __ENV.LOADTEST_PRIMARY_STORE_PRODUCT_ID || '1'
);

function getProductDetail(products, storeProductId) {
  return products[storeProductId] ?? products[String(storeProductId)] ?? null;
}

export const options = {
  vus: 1,
  iterations: 1,
};

export default function (data) {
  const products = data?.products || {};
  const productDetail = getProductDetail(products, PRIMARY_STORE_PRODUCT_ID);

  check(null, {
    'setup loaded products': () => Object.keys(products).length > 0,
    'primary SKU in snapshot': () => productDetail != null,
  });

  if (!productDetail) {
    return;
  }

  const payload = goldenOrder(productDetail);

  const validation = validateDeductPayload(payload, products);
  check(null, {
    'deduct payload valid': () => validation.valid,
  });
  if (!validation.valid) {
    console.error(validation.errors.join('; '));
    return;
  }

  const url = `${BASE_URL}/api/v1/loadtest/store/${STORE_ID}/inventory/deduct`;
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

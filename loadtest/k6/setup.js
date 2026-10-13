import http from 'k6/http';

function parseAllowlist() {
  const raw = __ENV.LOADTEST_STORE_PRODUCT_IDS;
  if (!raw || String(raw).trim() === '') {
    return null;
  }
  return String(raw)
    .split(',')
    .map((s) => Number(s.trim()))
    .filter((id) => Number.isFinite(id) && id > 0);
}

function flattenStoreProducts(categories) {
  const list = [];
  for (const cat of categories) {
    for (const p of cat.products || []) {
      list.push(p);
    }
  }
  return list;
}

function getJson(url, tagName) {
  const res = http.get(url, { tags: { name: tagName } });
  if (res.status !== 200) {
    throw new Error(`${tagName} status ${res.status}`);
  }
  return res.json();
}

export function setup() {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  const storeId = Number(__ENV.STORE_ID || '1');
  const allowlist = parseAllowlist();

  const categories = getJson(
    `${baseUrl}/api/v1/store/${storeId}/category/product`,
    'GET category/product'
  );
  if (!Array.isArray(categories)) {
    throw new Error('category/product: expected array');
  }

  let catalog = flattenStoreProducts(categories);
  if (allowlist && allowlist.length > 0) {
    const set = new Set(allowlist);
    catalog = catalog.filter((p) => set.has(p.storeProductId));
  }
  if (catalog.length === 0) {
    throw new Error('no store products (check LOADTEST_STORE_PRODUCT_IDS)');
  }

  const products = {};
  for (const item of catalog) {
    const id = item.storeProductId;
    const detail = getJson(
      `${baseUrl}/api/v1/store/${storeId}/product/${id}`,
      'GET product detail'
    );
    products[id] = detail;
  }

  console.log(`[setup] storeId=${storeId} products=${Object.keys(products).length}`);
  return { storeId, products };
}

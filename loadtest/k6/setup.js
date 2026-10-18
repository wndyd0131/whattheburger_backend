import http from 'k6/http';
import { weights } from './lib/weights.js';

function parseNameAllowlist() {
  const raw = __ENV.LOADTEST_PRODUCT_NAMES;
  if (!raw || String(raw).trim() === '') {
    return null;
  }
  return String(raw)
    .split(',')
    .map((s) => s.trim())
    .filter((name) => name.length > 0);
}

function productNamesFromWeights() {
  return Object.keys(weights.productsByName ?? {});
}

function resolveProductNames() {
  const envNames = parseNameAllowlist();
  if (envNames && envNames.length > 0) {
    return envNames;
  }
  const fromWeights = productNamesFromWeights();
  if (fromWeights.length === 0) {
    throw new Error(
      'weights.productsByName is empty; set LOADTEST_PRODUCT_NAMES or add SKU keys to weights.js'
    );
  }
  return fromWeights;
}

function resolveStoreIds() {
  const stores = weights.stores ?? {};
  const ids = [];
  for (const [key, w] of Object.entries(stores)) {
    const weight = Number(w);
    const id = Number(key);
    if (Number.isFinite(weight) && weight > 0 && Number.isFinite(id) && id > 0) {
      ids.push(id);
    }
  }
  if (ids.length === 0) {
    throw new Error(
      'weights.stores is empty; add store ids with positive weight in weights.js'
    );
  }
  ids.sort((a, b) => a - b);
  return ids;
}

function assertCatalogCoversNames(flatCatalog, requiredNames, storeId) {
  const found = new Set(flatCatalog.map((p) => p.name).filter(Boolean));
  const missing = requiredNames.filter((name) => !found.has(name));
  if (missing.length > 0) {
    throw new Error(
      `storeId=${storeId} catalog missing product name(s): ${missing.join(', ')}`
    );
  }
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

function loadStoreSnapshot(baseUrl, storeId, productNames) {
  const categories = getJson(
    `${baseUrl}/api/v1/store/${storeId}/category/product`,
    'GET category/product'
  );
  if (!Array.isArray(categories)) {
    throw new Error(`storeId=${storeId} category/product: expected array`);
  }

  const flatCatalog = flattenStoreProducts(categories);
  assertCatalogCoversNames(flatCatalog, productNames, storeId);
  const set = new Set(productNames);
  const catalog = flatCatalog.filter((p) => set.has(p.name));

  if (catalog.length === 0) {
    throw new Error(
      `storeId=${storeId} no store products (check LOADTEST_PRODUCT_NAMES or weights.productsByName)`
    );
  }

  const products = {};
  const storeProductIdByName = {};
  const resolvedPairs = [];

  for (const item of catalog) {
    const id = item.storeProductId;
    if (item.name) {
      storeProductIdByName[item.name] = id;
    }
    const detail = getJson(
      `${baseUrl}/api/v1/store/${storeId}/product/${id}`,
      'GET product detail'
    );
    products[id] = detail;
    const detailName = detail.productName;
    const logName = detailName || item.name || String(id);
    if (detailName) {
      storeProductIdByName[detailName] = id;
    }
    resolvedPairs.push(`${logName}→${id}`);
  }

  return { products, storeProductIdByName, resolvedPairs };
}

export function setup() {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  const storeIds = resolveStoreIds();
  const productNames = resolveProductNames();

  const byStore = {};
  const products = {};
  const logParts = [];

  for (const storeId of storeIds) {
    const snap = loadStoreSnapshot(baseUrl, storeId, productNames);
    byStore[storeId] = {
      products: snap.products,
      storeProductIdByName: snap.storeProductIdByName,
    };
    Object.assign(products, snap.products);
    logParts.push(
      `storeId=${storeId} loaded=${Object.keys(snap.products).length} (${snap.resolvedPairs.join(', ')})`
    );
  }

  console.log(`[setup] ${logParts.join('; ')}`);
  return { storeIds, byStore, products };
}

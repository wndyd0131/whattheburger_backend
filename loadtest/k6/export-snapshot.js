/**
 * One-shot: fetch catalog from API and write catalog-snapshot.json for SharedArray load in main.js.
 * Run from repo root: k6 run loadtest/k6/export-snapshot.js
 *
 * setup() (HTTP) → teardown(data) writes file in Init context (k6/experimental/fs).
 * Fallback: node loadtest/k6/bin/export-catalog-snapshot.mjs
 */
import { open } from 'k6/experimental/fs';
import { buildCatalogSnapshot } from './lib/buildCatalogSnapshot.js';

export function setup() {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  return buildCatalogSnapshot(baseUrl);
}

export default function () {}

export async function teardown(data) {
  if (data == null) {
    throw new Error(
      'export failed: setup returned no data (serialization failed or setup error)'
    );
  }
  const out =
    __ENV.LOADTEST_SNAPSHOT_OUT || 'loadtest/k6/fixtures/catalog-snapshot.json';
  const json = JSON.stringify(data, null, 2);
  const file = await open(out, 'w');
  try {
    await file.write(new TextEncoder().encode(json));
  } finally {
    await file.close();
  }
  console.log(`[export] wrote ${out} (${json.length} bytes)`);
}

export const options = {
  vus: 1,
  iterations: 1,
};

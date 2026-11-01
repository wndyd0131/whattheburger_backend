import { buildCatalogSnapshot } from './lib/buildCatalogSnapshot.js';
import { usesFileCatalog } from './lib/catalogSource.js';

/** Live HTTP setup (default). File mode skips HTTP — see LOADTEST_CATALOG_SOURCE=file. */
export function setup() {
  if (usesFileCatalog()) {
    return {};
  }
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  return buildCatalogSnapshot(baseUrl);
}

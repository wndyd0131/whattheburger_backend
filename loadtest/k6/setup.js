import { buildCatalogSnapshot } from './lib/buildCatalogSnapshot.js';
import { usesFileCatalog, usesFixturePayload } from './lib/catalogSource.js';
import { compactCatalog } from './lib/compactCatalog.js';

/** Live HTTP setup (default). File mode skips HTTP — see LOADTEST_CATALOG_SOURCE=file. */
export function setup() {
  if (usesFileCatalog() || usesFixturePayload()) {
    return {};
  }
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  return compactCatalog(buildCatalogSnapshot(baseUrl));
}

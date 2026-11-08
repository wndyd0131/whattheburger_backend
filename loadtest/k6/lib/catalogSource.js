/** setup (HTTP) vs file (SharedArray JSON). Default: setup. */

export function catalogSourceFromEnv() {
  const raw = __ENV.LOADTEST_CATALOG_SOURCE;
  if (raw == null || String(raw).trim() === '') {
    return 'setup';
  }
  const normalized = String(raw).trim().toLowerCase();
  if (normalized === 'file') {
    return 'file';
  }
  return 'setup';
}

export function usesFileCatalog() {
  return catalogSourceFromEnv() === 'file';
}

/** LOADTEST_FIXTURE=1: smoke mode, POST loadtest/fixtures/whattheburger-golden.json as-is. */
export function usesFixturePayload() {
  const raw = String(__ENV.LOADTEST_FIXTURE ?? '').trim().toLowerCase();
  return raw === '1' || raw === 'true';
}

export function usesSetupCatalog() {
  return !usesFileCatalog();
}

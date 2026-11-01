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

export function usesSetupCatalog() {
  return !usesFileCatalog();
}

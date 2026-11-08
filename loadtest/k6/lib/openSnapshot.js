const DEFAULT_SNAPSHOT = 'catalog-snapshot.json';

/**
 * k6 open() resolves paths from the process cwd, not the script directory.
 */
export function resolveSnapshotPath() {
  if (__ENV.LOADTEST_SNAPSHOT_PATH) {
    return String(__ENV.LOADTEST_SNAPSHOT_PATH).trim();
  }

  const candidates = [
    `loadtest/k6/fixtures/${DEFAULT_SNAPSHOT}`,
    `../fixtures/${DEFAULT_SNAPSHOT}`,
  ];

  for (const path of candidates) {
    try {
      open(path);
      return path;
    } catch (_) {
      // try next candidate
    }
  }

  throw new Error(
    `Catalog snapshot not found. Run: k6 run loadtest/k6/export-snapshot.js ` +
      `(or set LOADTEST_SNAPSHOT_PATH). Tried: ${candidates.join(', ')}`
  );
}

/** Raw JSON string from disk. */
export function openCatalogSnapshotJson() {
  const path = resolveSnapshotPath();
  return open(path);
}

/**
 * k6 open() resolves paths from the process cwd, not the script directory.
 * Tries repo-root path first, then loadtest/k6-relative ../fixtures.
 */
const GOLDEN_FIXTURE = 'whattheburger-golden.json';

export function openGoldenFixture() {
  if (__ENV.FIXTURE_PATH) {
    return open(__ENV.FIXTURE_PATH);
  }

  const candidates = [
    `loadtest/fixtures/${GOLDEN_FIXTURE}`,
    `../fixtures/${GOLDEN_FIXTURE}`,
  ];

  for (const path of candidates) {
    try {
      return open(path);
    } catch (_) {
      // try next candidate
    }
  }

  throw new Error(
    `Golden fixture not found. Set FIXTURE_PATH, or run from repo root ` +
      `(k6 run loadtest/k6/main.js) or from loadtest/k6 (k6 run main.js). ` +
      `Tried: ${candidates.join(', ')}`
  );
}

/**
 * LOADTEST_SEED: reproducible main.js picks only (store, lineCount, golden, SKU).
 * Generator option randomness always uses Math.random (see chanceUnseeded / weightedPick1Unseeded).
 */

const SEED_ENV = __ENV.LOADTEST_SEED;

function parseSeedFromEnv() {
  if (SEED_ENV == null || String(SEED_ENV).trim() === '') {
    return null;
  }
  const parsed = Number(String(SEED_ENV).trim());
  if (!Number.isFinite(parsed)) {
    return null;
  }
  return Math.trunc(parsed);
}

const FIXED_SEED = parseSeedFromEnv();
const SEEDED_MODE = FIXED_SEED != null;

function mulberry32(seed) {
  let state = seed >>> 0;
  return function next() {
    state = (state + 0x6d2b79f5) >>> 0;
    let t = state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

let rng = null;

function ensureRng() {
  if (rng != null) {
    return;
  }
  const vu = typeof __VU === 'number' ? __VU : 0;
  rng = mulberry32((FIXED_SEED + vu) >>> 0);
}

function seededUnitFloat() {
  if (!SEEDED_MODE) {
    return Math.random();
  }
  ensureRng();
  return rng();
}

export function isSeededMode() {
  return SEEDED_MODE;
}

export function chance(p) {
  return seededUnitFloat() < p;
}

export function chanceUnseeded(p) {
  return Math.random() < p;
}

/**
 * @param {Record<string, number>} weightByKey
 * @param {() => number} unitFloat
 * @returns {string|number|null}
 */
function weightedPick1With(unitFloat, weightByKey) {
  const keys = Object.keys(weightByKey);
  if (keys.length === 0) {
    return null;
  }

  let total = 0;
  const pairs = [];
  for (const key of keys) {
    const w = Number(weightByKey[key]);
    if (!Number.isFinite(w) || w <= 0) {
      continue;
    }
    pairs.push([key, w]);
    total += w;
  }

  if (pairs.length === 0) {
    const k = keys[Math.floor(unitFloat() * keys.length)];
    return Number.isNaN(Number(k)) ? k : Number(k);
  }

  let r = unitFloat() * total;
  for (const [key, w] of pairs) {
    r -= w;
    if (r <= 0) {
      return Number.isNaN(Number(key)) ? key : Number(key);
    }
  }
  const last = pairs[pairs.length - 1][0];
  return Number.isNaN(Number(last)) ? last : Number(last);
}

/** Seeded when LOADTEST_SEED is set (main.js: store, lineCount, golden, SKU). */
export function weightedPick1(weightByKey) {
  return weightedPick1With(seededUnitFloat, weightByKey);
}

/** Always Math.random() — generator.js option/shuffle paths. */
export function weightedPick1Unseeded(weightByKey) {
  return weightedPick1With(() => Math.random(), weightByKey);
}

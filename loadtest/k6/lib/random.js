export function chance(p) {
  return Math.random() < p;
}

/**
 * @param {Record<string, number>} weightByKey
 * @returns {string|number|null}
 */
export function weightedPick1(weightByKey) {
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
    const k = keys[Math.floor(Math.random() * keys.length)];
    return Number.isNaN(Number(k)) ? k : Number(k);
  }

  let r = Math.random() * total;
  for (const [key, w] of pairs) {
    r -= w;
    if (r <= 0) {
      return Number.isNaN(Number(key)) ? key : Number(key);
    }
  }
  const last = pairs[pairs.length - 1][0];
  return Number.isNaN(Number(last)) ? last : Number(last);
}

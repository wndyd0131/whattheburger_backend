import { SharedArray } from 'k6/data';
import { usesFileCatalog } from './catalogSource.js';
import { openCatalogSnapshotJson } from './openSnapshot.js';
import { compactCatalog } from './compactCatalog.js';

const wrapped = usesFileCatalog()
  ? new SharedArray('catalogSnapshot', () => [
      compactCatalog(JSON.parse(openCatalogSnapshotJson())),
    ])
  : null;

// SharedArray re-parses on every element access; keep one copy per VU.
let cached = null;

export function getCatalogSnapshot() {
  if (!wrapped) {
    return null;
  }
  if (cached == null) {
    cached = wrapped[0];
  }
  return cached;
}

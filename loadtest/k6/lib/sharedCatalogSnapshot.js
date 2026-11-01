import { SharedArray } from 'k6/data';
import { usesFileCatalog } from './catalogSource.js';
import { openCatalogSnapshotJson } from './openSnapshot.js';

const wrapped = usesFileCatalog()
  ? new SharedArray('catalogSnapshot', () => [
      JSON.parse(openCatalogSnapshotJson()),
    ])
  : null;

export function getCatalogSnapshot() {
  if (!wrapped) {
    return null;
  }
  return wrapped[0];
}

/**
 * Smoke test: linear happy path through 7 implemented APIs.
 *
 * 1. GET /store/nearby
 * 2. GET /store/{storeId}/category/product
 * 3. GET /store/{storeId}/product/{storeProductId}
 * 4. POST /store/{storeId}/cart
 * 5. GET /store/{storeId}/cart
 * 6. PATCH /store/{storeId}/cart/{cartIdx}/option
 * 7. POST /order-session/store/{storeId}
 */

import { sleep, fail } from 'k6';
import {
  RADIUS_METER,
  createApiClient,
  createGuestId,
  flattenStoreProducts,
  getUserLocation,
  isValidId,
  pickRandomCartItem,
  random,
  randomItem,
} from './common.js';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_failed: ['rate==0'],
    checks: ['rate==1'],
  },
};

const api = createApiClient({ failFast: true });

function think() {
  sleep(random(0.05, 0.15));
}

export default function () {
  const guestId = createGuestId();
  const location = getUserLocation();

  const stores = api.getStores(location.lon, location.lat, RADIUS_METER);
  if (stores.length === 0) {
    fail('no stores found in search radius');
  }

  const selectedStore = randomItem(stores);
  think();

  api.assertContext('selected store validation', { selectedStore, stores }, {
    'selected storeId is valid': (ctx) => isValidId(ctx.selectedStore.storeId),
    'selected storeId exists in list': (ctx) =>
      ctx.stores.some((s) => s.storeId === ctx.selectedStore.storeId),
  });

  const categories = api.getProducts(selectedStore.storeId);
  const storeProducts = flattenStoreProducts(categories);
  if (storeProducts.length === 0) {
    fail('no products found for store');
  }

  think();

  const product = randomItem(storeProducts);
  const productRes = api.getProduct(selectedStore.storeId, product.storeProductId);

  think();

  api.addCart(selectedStore.storeId, guestId, productRes.json());

  think();

  const cartRes = api.getCart(selectedStore.storeId, guestId);
  const picked = pickRandomCartItem(cartRes);
  if (picked) {
    think();
    const editProductRes = api.getProduct(
      selectedStore.storeId,
      picked.cartItem.storeProductId
    );
    think();
    api.updateCart(
      selectedStore.storeId,
      guestId,
      picked.cartIdx,
      editProductRes.json(),
      picked.cartItem.storeProductId
    );
  }

  think();

  api.createOrderSession(selectedStore.storeId, guestId);
}

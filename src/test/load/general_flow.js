/**
 * Load test: probabilistic user journey with churn.
 *
 * Implemented APIs:
 * 1. GET /store/nearby
 * 2. GET /store/{storeId}/category/product
 * 3. GET /store/{storeId}/product/{storeProductId}
 * 4. POST /store/{storeId}/cart
 * 5. GET /store/{storeId}/cart
 * 6. PATCH /store/{storeId}/cart/{cartIdx}/option
 * 7. POST /order-session/store/{storeId}
 */

import { sleep } from 'k6';
import {
  MAX_CART_ITEMS,
  RADIUS_METER,
  chance,
  createApiClient,
  createGuestId,
  flattenStoreProducts,
  getCartItemCount,
  getUserLocation,
  isValidId,
  pickRandomCartItem,
  random,
  randomItem,
} from './common.js';

export const options = {
  scenarios: {
    user_journey: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 10 },
        { duration: '3m', target: 10 },
        { duration: '1m', target: 25 },
        { duration: '3m', target: 25 },
        { duration: '1m', target: 50 },
        { duration: '3m', target: 50 },
        { duration: '1m', target: 0 },
      ],
      gracefulRampDown: '30s',
    },
  },

  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

const api = createApiClient({ failFast: false });

function think(min, max) {
  sleep(random(min, max));
}

export default function () {
  const guestId = createGuestId();

  /* ---------------------------------------------------------
   * 1. 매장 탐색
   * ------------------------------------------------------- */

  const location = getUserLocation();

  let selectedStore = null;
  let stores = [];

  for (let i = 0; i < 3; i++) {
    stores = api.getStores(
      location.lon,
      location.lat,
      RADIUS_METER
    );

    if (!stores || stores.length === 0) {
      return;
    }

    think(5, 15);

    if (chance(0.20)) {
      return;
    }

    if (chance(0.50) || i === 2) {
      selectedStore = randomItem(stores);
      break;
    }
  }

  if (!selectedStore) {
    return;
  }

  api.assertContext('selected store validation', { selectedStore, stores }, {
    'selected storeId is valid': (ctx) => isValidId(ctx.selectedStore.storeId),
    'selected storeId exists in list': (ctx) =>
      ctx.stores.some((s) => s.storeId === ctx.selectedStore.storeId),
  });


  /* ---------------------------------------------------------
   * 2. 상품 목록
   * ------------------------------------------------------- */

  const categories = api.getProducts(selectedStore.storeId);
  let storeProducts = flattenStoreProducts(categories);

  if (storeProducts.length === 0) {
    return;
  }

  if (chance(0.30)) {
    return;
  }


  /* ---------------------------------------------------------
   * 3. 상품 탐색
   *
   * 최대 10번
   * ------------------------------------------------------- */

  let hasCartItem = false;

  for (let exploration = 0; exploration < 10; exploration++) {
    think(10, 15);

    const product = randomItem(storeProducts);

    const productRes = api.getProduct(selectedStore.storeId, product.storeProductId);

    think(20, 30);

    const action = Math.random();

    if (action < 0.50) {
      storeProducts = flattenStoreProducts(api.getProducts(selectedStore.storeId));

      if (storeProducts.length === 0) {
        return;
      }

      continue;
    }

    if (action < 0.80) {
      api.addCart(selectedStore.storeId, guestId, productRes.json());

      hasCartItem = true;

      break;
    }

    return;
  }

  if (!hasCartItem) {
    return;
  }


  /* ---------------------------------------------------------
   * 4. 장바구니
   * ------------------------------------------------------- */

  while (true) {
    const cartRes = api.getCart(selectedStore.storeId, guestId);
    const cartItemCount = getCartItemCount(cartRes);

    think(10, 20);

    const action = Math.random();

    if (action < 0.20) {
      const picked = pickRandomCartItem(cartRes);
      if (!picked) {
        continue;
      }

      const productRes = api.getProduct(selectedStore.storeId, picked.cartItem.storeProductId);

      think(10, 20);

      if (chance(0.50)) {
        api.updateCart(
          selectedStore.storeId,
          guestId,
          picked.cartIdx,
          productRes.json(),
          picked.cartItem.storeProductId
        );
      }

      continue;
    }

    if (action < 0.45) {
      storeProducts = flattenStoreProducts(api.getProducts(selectedStore.storeId));

      if (storeProducts.length === 0) {
        return;
      }

      think(10, 20);

      const product = randomItem(storeProducts);

      const productRes = api.getProduct(selectedStore.storeId, product.storeProductId);

      think(10, 15);

      if (chance(0.30) && cartItemCount < MAX_CART_ITEMS) {
        api.addCart(selectedStore.storeId, guestId, productRes.json());
      }

      continue;
    }

    if (action < 0.90) {
      break;
    }

    return;
  }


  /* ---------------------------------------------------------
   * 5. Checkout
   * ------------------------------------------------------- */

  if (chance(0.60)) {
    return;
  }

  api.createOrderSession(selectedStore.storeId, guestId);
}

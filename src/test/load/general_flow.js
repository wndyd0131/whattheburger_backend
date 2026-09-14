	/**
    1. `GET /store/nearby`
    2. `GET /store/{storeId}/category/product`
    3. `GET /store/{storeId}/product/{storeProductId}`
    4. `POST /store/{storeId}/cart`
    5. `PATCH /store/{storeId}/cart/{cartIdx}/option`
    6. `PATCH /store/{storeId}/cart/{cartIdx}/product`
    7. `POST /order-session/store/{storeId}`
    8. `POST /checkout/{orderSessionId}`
    9. `POST /checkout/webhook`
   */

import http from 'k6/http';
import exec from 'k6/execution';
import { check, sleep, fail } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

/* =========================================================
 * Configuration
 * ======================================================= */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// export const options = {
//   scenarios: {
//     user_journey: {
//       executor: 'ramping-vus',
//       startVUs: 0,
//       stages: [
//         { duration: '1m', target: 10 },
//         { duration: '3m', target: 10 },
//         { duration: '1m', target: 25 },
//         { duration: '3m', target: 25 },
//         { duration: '1m', target: 50 },
//         { duration: '3m', target: 50 },
//         { duration: '1m', target: 0 },
//       ],
//       gracefulRampDown: '30s',
//     },
//   },

//   thresholds: {
//     http_req_failed: ['rate<0.01'],
//     http_req_duration: ['p(95)<1000'],
//   },
// };

/* =========================================================
 * Parameters
 * ======================================================= */

const USER_LOCATIONS = [
  // Store 1: -97.8257541, 30.5149854
  { lon: -97.8102, lat: 30.5274 },
  { lon: -97.8478, lat: 30.4931 },

  // Store 2: -97.7906425, 30.4695626
  { lon: -97.7731, lat: 30.4828 },
  { lon: -97.8154, lat: 30.4517 },

  // Store 3: -96.8950251, 32.7645228
  { lon: -96.8783, lat: 32.7789 },
  { lon: -96.9207, lat: 32.7462 },

  // Store 4: -97.7871906, 30.5327327
  { lon: -97.7686, lat: 30.5461 },
  { lon: -97.8121, lat: 30.5134 },

  // Store 5: -95.4088926, 29.9415036
  { lon: -95.3904, lat: 29.9567 },
  { lon: -95.4341, lat: 29.9238 },

  // Store 6: -86.8218093, 35.9421571
  { lon: -86.8016, lat: 35.9564 },
  { lon: -86.8473, lat: 35.9231 },

  // Store 7: -82.3170866, 34.7762055
  { lon: -82.2981, lat: 34.7908 },
  { lon: -82.3427, lat: 34.7574 },

  // Store 8: -82.3064777, 34.8306646
  { lon: -82.2874, lat: 34.8452 },
  { lon: -82.3321, lat: 34.8118 },

  // Store 9: -97.8474367, 30.1829816
  { lon: -97.8285, lat: 30.1977 },
  { lon: -97.8723, lat: 30.1641 },

  // Store 10: -97.3167542, 32.8606535
  { lon: -97.2968, lat: 32.8751 },
  { lon: -97.3426, lat: 32.8422 },

  // Store 11: -106.7475664, 32.3174868
  { lon: -106.7281, lat: 32.3322 },
  { lon: -106.7734, lat: 32.2986 },

  // Store 12: -95.5587591, 29.7357579
  { lon: -95.5397, lat: 29.7504 },
  { lon: -95.5842, lat: 29.7169 },

  // Store 13: -97.7879910, 30.1661403
  { lon: -97.7688, lat: 30.1808 },
  { lon: -97.8131, lat: 30.1474 },

  // Store 14: -106.7687111, 32.2819090
  { lon: -106.7492, lat: 32.2965 },
  { lon: -106.7943, lat: 32.2630 },

  // Store 15: -82.2645663, 34.8591624
  { lon: -82.2453, lat: 34.8738 },
  { lon: -82.2901, lat: 34.8405 },
];

const RADIUS_METER = 5000;
const MAX_CART_ITEMS = 20;


/* =========================================================
 * Utility
 * ======================================================= */

function random(min, max) {
  return Math.random() * (max - min) + min;
}

function chance(probability) {
  return Math.random() < probability;
}

function randomItem(array) {
  return array[Math.floor(Math.random() * array.length)];
}

function flattenStoreProducts(categorizedProducts) {
  const storeProducts = [];
  for (const category of categorizedProducts) {
    for (const product of category.products || []) {
      storeProducts.push(product);
    }
  }
  return storeProducts;
}

function think(min, max) {
  sleep(random(min, max));
}

function getUserLocation() {
  const index = (exec.vu.idInTest - 1) % USER_LOCATIONS.length;
  return USER_LOCATIONS[index];
}

function createGuestId() {
  return uuidv4();
}

function withGuestCookie(guestId, headers = {}) {
  return {
    ...headers,
    Cookie: `guestId=${guestId}`,
  };
}

function isValidId(value) {
  return value != null && Number(value) > 0;
}

function isValidUuid(value) {
  return typeof value === 'string'
    && /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);
}

function runStepChecks(step, res, checks) {
  const ok = check(res, checks);
  if (!ok) {
    console.error(`[SMOKE FAIL] ${step}`);
    console.error(`  status: ${res.status}`);
    console.error(`  body: ${res.body}`);
    fail(step);
  }
  return ok;
}

function runAssertions(step, context, checks) {
  const ok = check(context, checks);
  if (!ok) {
    console.error(`[SMOKE FAIL] ${step}`);
    console.error(`  context: ${JSON.stringify(context)}`);
    fail(step);
  }
  return ok;
}

function randomInt(min, max) {
  return Math.floor(random(min, max + 1));
}

function shuffle(array) {
  const copy = array.slice();
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    const temp = copy[i];
    copy[i] = copy[j];
    copy[j] = temp;
  }
  return copy;
}

function pickRandomSubset(array, count) {
  return shuffle(array).slice(0, count);
}


/* =========================================================
 * Cart Payload Builder
 * ======================================================= */

function normalizeProductDetail(productDetail) {
  const optionResponses = productDetail.optionResponses || [];
  const ruleMap = new Map();

  for (const option of optionResponses) {
    const rule = option.customRuleResponse;
    if (!rule) {
      continue;
    }

    const ruleId = rule.customRuleId;
    if (!ruleMap.has(ruleId)) {
      ruleMap.set(ruleId, {
        customRuleId: rule.customRuleId,
        customRuleType: rule.customRuleType,
        minSelection: rule.minSelection,
        maxSelection: rule.maxSelection,
        orderIndex: rule.orderIndex,
        optionRequests: [],
      });
    }

    const quantityDetails = (option.quantityDetailResponses || []).map((qd) => ({
      id: qd.id,
    }));
    const optionTraitRequests = (option.optionTraitResponses || []).map((trait) => ({
      productOptionTraitId: trait.productOptionTraitId,
      optionTraitType: trait.optionTraitType,
      defaultSelection: trait.defaultSelection,
    }));

    ruleMap.get(ruleId).optionRequests.push({
      productOptionId: option.productOptionId,
      countType: option.countType,
      maxQuantity: option.maxQuantity,
      defaultQuantity: option.defaultQuantity,
      orderIndex: option.orderIndex,
      quantityDetails,
      optionTraitRequests,
    });
  }

  const customRuleRequests = Array.from(ruleMap.values())
    .sort((a, b) => a.orderIndex - b.orderIndex)
    .map((rule) => ({
      ...rule,
      optionRequests: rule.optionRequests.sort((a, b) => a.orderIndex - b.orderIndex),
    }));

  return {
    storeProductId: productDetail.storeProductId,
    customRuleRequests,
  };
}

function pickSelectionCount(customRule) {
  const optionCount = customRule.optionRequests.length;
  if (optionCount === 0) {
    return 0;
  }

  const min = customRule.minSelection ?? 0;
  const max = customRule.maxSelection ?? optionCount;

  switch (customRule.customRuleType) {
    case 'UNIQUE':
      return 1;
    case 'LIMIT': {
      const upper = Math.min(max, optionCount);
      const lower = Math.min(min, upper);
      return randomInt(lower, upper);
    }
    case 'FREE': {
      const upper = Math.min(max, optionCount);
      const lower = Math.min(min, upper);
      return randomInt(lower, upper);
    }
    default:
      return randomInt(Math.min(min, optionCount), Math.min(max, optionCount));
  }
}

function buildTraitRequests(traits, isSelected) {
  if (!isSelected || !traits || traits.length === 0) {
    return [];
  }

  return traits.map((trait) => ({
    productOptionTraitId: trait.productOptionTraitId,
    currentValue: trait.optionTraitType === 'BINARY'
      ? randomInt(0, 1)
      : randomInt(0, 1),
  }));
}

function buildOptionRequest(option, isSelected) {
  const request = {
    productOptionId: option.productOptionId,
    isSelected,
    optionTraitRequests: buildTraitRequests(option.optionTraitRequests, isSelected),
    optionQuantity: null,
    quantityDetailRequest: null,
  };

  if (!isSelected) {
    return request;
  }

  switch (option.countType) {
    case 'COUNTABLE':
      request.optionQuantity = randomInt(1, option.maxQuantity || 1);
      break;
    case 'UNCOUNTABLE': {
      const details = option.quantityDetails || [];
      if (details.length > 0) {
        request.quantityDetailRequest = { id: randomItem(details).id };
      }
      break;
    }
    case 'NONE':
    default:
      break;
  } //

  return request;
}

function buildCustomRuleRequest(customRule) {
  const options = customRule.optionRequests;
  if (options.length === 0) {
    return {
      customRuleId: customRule.customRuleId,
      optionRequests: [],
    };
  }

  if (customRule.customRuleType === 'UNIQUE') {
    const selected = randomItem(options);
    return {
      customRuleId: customRule.customRuleId,
      optionRequests: options.map((option) =>
        buildOptionRequest(option, option.productOptionId === selected.productOptionId)
      ),
    };
  }

  const count = pickSelectionCount(customRule);
  const selectedOptions = pickRandomSubset(options, count); // count 만큼 랜덤으로 선택

  return {
    customRuleId: customRule.customRuleId,
    optionRequests: selectedOptions.map((option) => buildOptionRequest(option, true)),
  };
}

function buildRandomCartPayload(product) {
  const customRuleRequests = (product.customRuleRequests || [])
    .filter((rule) => rule.optionRequests && rule.optionRequests.length > 0)
    .map(buildCustomRuleRequest);

  return {
    storeProductId: product.storeProductId,
    quantity: randomInt(1, 5),
    customRuleRequests,
  };
}

function buildRandomOptionModifyPayload(productDetail) {
  return {
    customRuleRequests: buildRandomCartPayload(
      normalizeProductDetail(productDetail)
    ).customRuleRequests,
  };
}


/* =========================================================
 * API
 * ======================================================= */

function getStores(lon, lat, radiusMeter) {
  const res = http.get(`${BASE_URL}/store/nearby?lon=${lon}&lat=${lat}&radiusMeter=${radiusMeter}`, {
    tags: { name: 'GET /store/nearby' },
  });

  runStepChecks('GET /store/nearby', res, {
    'status is 200': (r) => r.status === 200,
    'body is array': (r) => {
      try {
        return Array.isArray(r.json());
      } catch (e) {
        return false;
      }
    },
  });

  const stores = res.json();
  console.log(`[GET /store/nearby] stores count: ${stores.length}`);
  return stores;
}


function getProducts(storeId) {
  const res = http.get(`${BASE_URL}/store/${storeId}/category/product`, {
    tags: { name: 'GET /store/{storeId}/category/product' },
  });

  runStepChecks('GET /store/{storeId}/category/product', res, {
    'status is 200': (r) => r.status === 200,
    'body is array': (r) => {
      try {
        return Array.isArray(r.json());
      } catch (e) {
        return false;
      }
    },
  });

  const categories = res.json();
  console.log(`[GET /store/{storeId}/category/product] categories count: ${categories.length}`);
  return categories;
}


function getProduct(storeId, storeProductId) {
  const res = http.get(`${BASE_URL}/store/${storeId}/product/${storeProductId}`, {
    tags: { name: 'GET /store/{storeId}/product/{storeProductId}' },
  });

  runStepChecks('GET /store/{storeId}/product/{storeProductId}', res, {
    'status is 200': (r) => r.status === 200,
    'storeProductId is valid and matches': (r) => {
      try {
        const body = r.json();
        return isValidId(body.storeProductId) && body.storeProductId === storeProductId;
      } catch (e) {
        return false;
      }
    },
  });

  return res;
}


function addCart(storeId, guestId, productDetail) {
  const payload = buildRandomCartPayload(normalizeProductDetail(productDetail));

  runAssertions('POST /store/{storeId}/cart payload', { payload }, {
    'storeProductId is valid': (ctx) => isValidId(ctx.payload.storeProductId),
  });

  const res = http.post(`${BASE_URL}/store/${storeId}/cart`, JSON.stringify(payload), {
    headers: withGuestCookie(guestId, {
      'Content-Type': 'application/json',
    }),
    tags: { name: 'POST /store/{storeId}/cart' },
  });

  runStepChecks('POST /store/{storeId}/cart', res, {
    'status is 201': (r) => r.status === 201,
    'response storeProductId matches': (r) => {
      try {
        const body = r.json();
        return body.storeProductId === payload.storeProductId;
      } catch (e) {
        return false;
      }
    },
  });

  return res;
}


function getCart(storeId, guestId) {
  const res = http.get(`${BASE_URL}/store/${storeId}/cart`, {
    headers: withGuestCookie(guestId),
    tags: { name: 'GET /store/{storeId}/cart' },
  });

  check(res, {
    'cart: 200': (r) => r.status === 200,
  });

  return res;
}

function getCartItemCount(cartRes) {
  if (cartRes.status !== 200) {
    return 0;
  }

  const cart = cartRes.json();
  return (cart.productResponses || []).length;
}

function pickRandomCartItem(cartRes) {
  if (cartRes.status !== 200) {
    return null;
  }

  const items = cartRes.json().productResponses || [];
  if (items.length === 0) {
    return null;
  }

  const cartIdx = randomInt(0, items.length - 1);
  return { cartIdx, cartItem: items[cartIdx] };
}


function updateCart(storeId, guestId, cartIdx, productDetail, expectedStoreProductId) {

  const payload = buildRandomOptionModifyPayload(productDetail);

  const res = http.patch(
    `${BASE_URL}/store/${storeId}/cart/${cartIdx}/option`,
    JSON.stringify(payload),
    {
      headers: withGuestCookie(guestId, {
        'Content-Type': 'application/json',
      }),
      tags: { name: 'PATCH /store/{storeId}/cart/{cartIdx}/option' },
    }
  );

  runStepChecks('PATCH /store/{storeId}/cart/{cartIdx}/option', res, {
    'status is 200': (r) => r.status === 200,
    'response storeProductId matches': (r) => {
      try {
        const body = r.json();
        const items = body.productResponses || [];
        return items[cartIdx] != null
          && items[cartIdx].storeProductId === expectedStoreProductId;
      } catch (e) {
        return false;
      }
    },
  });

  return res;
}


function createOrderSession(storeId, guestId) {
  const res = http.post(
    `${BASE_URL}/order-session/store/${storeId}`,
    JSON.stringify({ orderType: 'DELIVERY' }),
    {
      headers: withGuestCookie(guestId, {
        'Content-Type': 'application/json',
      }),
      tags: { name: 'POST /order-session/store/{storeId}' },
    }
  );

  runStepChecks('POST /order-session/store/{storeId}', res, {
    'status is 201': (r) => r.status === 201,
    'sessionId is valid': (r) => {
      try {
        const body = r.json();
        return isValidUuid(String(body.sessionId));
      } catch (e) {
        return false;
      }
    },
  });

  return res;
}


/* =========================================================
 * User Journey
 * ======================================================= */

export default function () {
  const guestId = createGuestId();

  /* ---------------------------------------------------------
   * 1. 매장 탐색
   * ------------------------------------------------------- */

  const location = getUserLocation();

  let selectedStore = null;
  let stores = [];

  for (let i = 0; i < 3; i++) {
    stores = getStores(
      location.lon,
      location.lat,
      RADIUS_METER
    );

    // 배달 가능 지역 없어서 이탈
    if (stores.length === 0) {
      return;
    }

    // 매장 살펴보는 시간
    think(60, 90);

    // 20% 이탈
    if (chance(0.20)) {
      return;
    }

    /*
     * 80%는 다시 조회하거나 매장 선택.
     *
     * 여기서는 단순화를 위해
     * 50% 선택 / 50% 다시 조회.
     */
    if (chance(0.50) || i === 2) {
      selectedStore = randomItem(stores);
      break;
    }
  }

  if (!selectedStore) {
    return;
  }

  runAssertions('selected store validation', { selectedStore, stores }, {
    'selected storeId is valid': (ctx) => isValidId(ctx.selectedStore.storeId),
    'selected storeId exists in list': (ctx) =>
      ctx.stores.some((s) => s.storeId === ctx.selectedStore.storeId),
  });


  /* ---------------------------------------------------------
   * 2. 상품 목록
   * ------------------------------------------------------- */

  const categories = getProducts(selectedStore.storeId);
  let storeProducts = flattenStoreProducts(categories);
  console.log(`[GET /store/{storeId}/category/product] products count: ${storeProducts.length}`);

  if (storeProducts.length === 0) {
    return;
  }

  // 30% 이탈
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

    // 상품 고르는 시간
    think(10, 60);

    const product = randomItem(storeProducts);

    const productRes = getProduct(selectedStore.storeId, product.storeProductId);

    // 상품 상세를 보는 시간
    think(5, 120);


    const action = Math.random();

    /*
     * 50% 뒤로가기
     */
    if (action < 0.50) {
      // 다시 상품 목록 조회
      storeProducts = flattenStoreProducts(getProducts(selectedStore.storeId));

      if (storeProducts.length === 0) {
        return;
      }

      continue;
    }


    /*
     * 30% 장바구니 담기
     *
     * 0.50 ~ 0.80
     */
    if (action < 0.80) {
      addCart(selectedStore.storeId, guestId, productRes.json());

      hasCartItem = true;

      break;
    }


    /*
     * 나머지 20% 이탈
     */
    return;
  }


  /*
   * 10번 탐색했는데 아무것도 담지 않았다면 종료
   */
  if (!hasCartItem) {
    return;
  }


  /* ---------------------------------------------------------
   * 4. 장바구니
   * ------------------------------------------------------- */

  while (true) {

    const cartRes = getCart(selectedStore.storeId, guestId);
    const cartItemCount = getCartItemCount(cartRes);

    think(10, 20);

    const action = Math.random();


    /*
     * 20% 수정 화면 진입
     */
    if (action < 0.20) {
      const picked = pickRandomCartItem(cartRes);
      if (!picked) {
        continue;
      }

      const productRes = getProduct(selectedStore.storeId, picked.cartItem.storeProductId);

      // 수정 화면에서 옵션 살펴보는 시간
      think(10, 60);

      /*
       * 50% 수정 확정
       */
      if (chance(0.50)) {
        updateCart(
          selectedStore.storeId,
          guestId,
          picked.cartIdx,
          productRes.json(),
          picked.cartItem.storeProductId
        );
      }

      /*
       * 나머지 50%는 수정 취소
       *
       * 둘 다 다시 장바구니 조회
       */
      continue;
    }


    /*
     * 25% 다시 상품 탐색
     *
     * 0.20 ~ 0.45
     */
    if (action < 0.45) {

      storeProducts = flattenStoreProducts(getProducts(selectedStore.storeId));

      if (storeProducts.length === 0) {
        return;
      }

      /*
       * 간단하게 상품 하나 추가 탐색
       *
       * 필요하면 위의 product exploration을
       * 함수로 빼서 재사용하면 됨.
       */
      think(10, 60);

      const product = randomItem(storeProducts);

      const productRes = getProduct(selectedStore.storeId, product.storeProductId);

      think(5, 120);

      /*
       * 여기서는 30% 확률로 추가 상품 담기
       * (장바구니 최대 20개)
       */
      if (chance(0.30) && cartItemCount < MAX_CART_ITEMS) {
        addCart(selectedStore.storeId, guestId, productRes.json());
      }

      continue;
    }


    /*
     * 45% Checkout
     *
     * 0.45 ~ 0.90
     */
    if (action < 0.90) {
      break;
    }


    /*
     * 10% 이탈
     */
    return;
  }


  /* ---------------------------------------------------------
   * 5. Checkout
   * ------------------------------------------------------- */

  // 주소/정보 입력 등을 가정
  think(60, 180);

  // 60% 이탈
  if (chance(0.60)) {
    return;
  }


  /*
   * 40% 결제 진행
   *
   * Stripe Checkout Session 생성까지만 수행.
   */
  createOrderSession(selectedStore.storeId, guestId);
}
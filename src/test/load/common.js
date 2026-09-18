import http from 'k6/http';
import exec from 'k6/execution';
import { check, fail } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const USER_LOCATIONS = [
  { lon: -97.8102, lat: 30.5274 },
  { lon: -97.8478, lat: 30.4931 },
  { lon: -97.7731, lat: 30.4828 },
  { lon: -97.8154, lat: 30.4517 },
  { lon: -96.8783, lat: 32.7789 },
  { lon: -96.9207, lat: 32.7462 },
  { lon: -97.7686, lat: 30.5461 },
  { lon: -97.8121, lat: 30.5134 },
  { lon: -95.3904, lat: 29.9567 },
  { lon: -95.4341, lat: 29.9238 },
  { lon: -86.8016, lat: 35.9564 },
  { lon: -86.8473, lat: 35.9231 },
  { lon: -82.2981, lat: 34.7908 },
  { lon: -82.3427, lat: 34.7574 },
  { lon: -82.2874, lat: 34.8452 },
  { lon: -82.3321, lat: 34.8118 },
  { lon: -97.8285, lat: 30.1977 },
  { lon: -97.8723, lat: 30.1641 },
  { lon: -97.2968, lat: 32.8751 },
  { lon: -97.3426, lat: 32.8422 },
  { lon: -106.7281, lat: 32.3322 },
  { lon: -106.7734, lat: 32.2986 },
  { lon: -95.5397, lat: 29.7504 },
  { lon: -95.5842, lat: 29.7169 },
  { lon: -97.7688, lat: 30.1808 },
  { lon: -97.8131, lat: 30.1474 },
  { lon: -106.7492, lat: 32.2965 },
  { lon: -106.7943, lat: 32.2630 },
  { lon: -82.2453, lat: 34.8738 },
  { lon: -82.2901, lat: 34.8405 },
];

export const RADIUS_METER = 5000;
export const MAX_CART_ITEMS = 20;

export function random(min, max) {
  return Math.random() * (max - min) + min;
}

export function chance(probability) {
  return Math.random() < probability;
}

export function randomItem(array) {
  return array[Math.floor(Math.random() * array.length)];
}

export function flattenStoreProducts(categorizedProducts) {
  const storeProducts = [];
  for (const category of categorizedProducts) {
    for (const product of category.products || []) {
      storeProducts.push(product);
    }
  }
  return storeProducts;
}

export function getUserLocation() {
  const index = (exec.vu.idInTest - 1) % USER_LOCATIONS.length;
  return USER_LOCATIONS[index];
}

export function createGuestId() {
  return uuidv4();
}

export function withGuestCookie(guestId, headers = {}) {
  return {
    ...headers,
    Cookie: `guestId=${guestId}`,
  };
}

export function isValidId(value) {
  return value != null && Number(value) > 0;
}

export function isValidUuid(value) {
  return typeof value === 'string'
    && /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);
}

export function runStepChecks(step, res, checks) {
  const ok = check(res, checks);
  if (!ok) {
    console.error(`[SMOKE FAIL] ${step}`);
    console.error(`  status: ${res.status}`);
    console.error(`  body: ${res.body}`);
    fail(step);
  }
  return ok;
}

export function runAssertions(step, context, checks) {
  const ok = check(context, checks);
  if (!ok) {
    console.error(`[SMOKE FAIL] ${step}`);
    console.error(`  context: ${JSON.stringify(context)}`);
    fail(step);
  }
  return ok;
}

export function logApiCall(step, res, extra = {}) {
  const duration = res.timings && res.timings.duration != null
    ? `${Math.round(res.timings.duration)}ms`
    : 'n/a';
  const extraStr = Object.keys(extra).length > 0 ? ` ${JSON.stringify(extra)}` : '';
  console.log(`[API] ${step} status=${res.status} duration=${duration}${extraStr}`);
}

export function randomInt(min, max) {
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
  }

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
  const selectedOptions = pickRandomSubset(options, count);

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

export function getCartItemCount(cartRes) {
  if (cartRes.status !== 200) {
    return 0;
  }

  const cart = cartRes.json();
  return (cart.productResponses || []).length;
}

export function pickRandomCartItem(cartRes) {
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

export function createApiClient({ failFast }) {
  function validate(step, res, checks) {
    if (failFast) {
      runStepChecks(step, res, checks);
    } else {
      check(res, checks);
    }
  }

  function assertContext(step, context, checks) {
    if (failFast) {
      runAssertions(step, context, checks);
    } else {
      check(context, checks);
    }
  }

  function getStores(lon, lat, radiusMeter) {
    const step = 'GET /store/nearby';
    const res = http.get(`${BASE_URL}/store/nearby?lon=${lon}&lat=${lat}&radiusMeter=${radiusMeter}`, {
      tags: { name: step },
    });

    logApiCall(step, res, { lon, lat, radiusMeter });

    validate(step, res, {
      'status is 200': (r) => r.status === 200,
      'body is array': (r) => {
        try {
          return Array.isArray(r.json());
        } catch (e) {
          return false;
        }
      },
    });

    return res.json();
  }

  function getProducts(storeId) {
    const step = 'GET /store/{storeId}/category/product';
    const res = http.get(`${BASE_URL}/store/${storeId}/category/product`, {
      tags: { name: step },
    });

    logApiCall(step, res, { storeId });

    validate(step, res, {
      'status is 200': (r) => r.status === 200,
      'body is array': (r) => {
        try {
          return Array.isArray(r.json());
        } catch (e) {
          return false;
        }
      },
    });

    return res.json();
  }

  function getProduct(storeId, storeProductId) {
    const step = 'GET /store/{storeId}/product/{storeProductId}';
    const res = http.get(`${BASE_URL}/store/${storeId}/product/${storeProductId}`, {
      tags: { name: step },
    });

    logApiCall(step, res, { storeId, storeProductId });

    validate(step, res, {
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
    const step = 'POST /store/{storeId}/cart';
    const payload = buildRandomCartPayload(normalizeProductDetail(productDetail));

    assertContext(`${step} payload`, { payload }, {
      'storeProductId is valid': (ctx) => isValidId(ctx.payload.storeProductId),
    });

    const res = http.post(`${BASE_URL}/store/${storeId}/cart`, JSON.stringify(payload), {
      headers: withGuestCookie(guestId, {
        'Content-Type': 'application/json',
      }),
      tags: { name: step },
    });

    logApiCall(step, res, { storeId, storeProductId: payload.storeProductId, quantity: payload.quantity });

    validate(step, res, {
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
    const step = 'GET /store/{storeId}/cart';
    const res = http.get(`${BASE_URL}/store/${storeId}/cart`, {
      headers: withGuestCookie(guestId),
      tags: { name: step },
    });

    logApiCall(step, res, { storeId });

    validate(step, res, {
      'status is 200': (r) => r.status === 200,
      'productResponses is array': (r) => {
        try {
          return Array.isArray(r.json().productResponses);
        } catch (e) {
          return false;
        }
      },
    });

    return res;
  }

  function updateCart(storeId, guestId, cartIdx, productDetail, expectedStoreProductId) {
    const step = 'PATCH /store/{storeId}/cart/{cartIdx}/option';
    const payload = buildRandomOptionModifyPayload(productDetail);

    const res = http.patch(
      `${BASE_URL}/store/${storeId}/cart/${cartIdx}/option`,
      JSON.stringify(payload),
      {
        headers: withGuestCookie(guestId, {
          'Content-Type': 'application/json',
        }),
        tags: { name: step },
      }
    );

    logApiCall(step, res, { storeId, cartIdx, expectedStoreProductId });

    validate(step, res, {
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
    const step = 'POST /order-session/store/{storeId}';
    const res = http.post(
      `${BASE_URL}/order-session/store/${storeId}`,
      JSON.stringify({ orderType: 'DELIVERY' }),
      {
        headers: withGuestCookie(guestId, {
          'Content-Type': 'application/json',
        }),
        tags: { name: step },
      }
    );

    logApiCall(step, res, { storeId, orderType: 'DELIVERY' });

    validate(step, res, {
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

  return {
    assertContext,
    getStores,
    getProducts,
    getProduct,
    addCart,
    getCart,
    updateCart,
    createOrderSession,
  };
}

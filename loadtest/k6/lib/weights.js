/**
 * Load-test sampling weights (edit numbers here).
 * Keys use catalog productName (GET product detail), not storeProductId — setup resolves id per store.
 *
 * UNIQUE (Bread, Beef, Included Cheese, …): options[id] = relative weight — pick exactly one.
 * FREE / LIMIT: options[id] = { p } — P(include this option | rule modify path). (Values below were converted from omit-q: p = 1 - q.)
 * modify: P(customize this rule) under byProductName[productName][ruleName].
 *
 * productOptionId values match the Product entity (stable across stores for the same menu item).
 */
export const weights = {
  /** P(goldenOrder) per iteration in main.js; override with LOADTEST_GOLDEN_RATIO. */
  goldenRatio: 0.7,

  /** Relative weight per storeId — setup loads each key; main picks one per iteration. */
  stores: {
    1: 0.25,
    2: 0.2,
    3: 0.15,
    4: 0.1,
    5: 0.08,
    6: 0.06,
    7: 0.05,
    8: 0.04,
    9: 0.04,
    10: 0.03
  },

  order: {
    /** Relative weight for orderProducts.length per deduct request. */
    lineCount: { 1: 0.55, 2: 0.30, 3: 0.15 }
  },

  /** Relative weight when picking a line SKU (productName). */
  productsByName: {
    'Whattheburger': 0.3,
    'French Fries': 0.2,
    'Whatthechicken': 0.2,
    'Bacon & Cheese Whattheburger': 0.14,
    'Whatthechicken Strips 3 pcs': 0.1,
    'Patty Melt': 0.06
  },

  /** Fallback when byProductName[name][rule].modify is omitted. */
  modifyRule: {
    Bread: 0,
    Beef: 0,
    Cheese: 0,
    'Included Cheese': 0,
    'Popular Add-ons': 0,
    Toppings: 0,
    Sauces: 0,
  },

  byProductName: {
    Whattheburger: {
      Bread: {
        modify: 0.5,
        options: {
          1: 0.2,
          2: 0.2,
          3: 0.25,
          4: 0.25,
          5: 0.1,
        },
      },
      Beef: {
        modify: 0.5,
        options: {
          6: 1.0,
        },
      },
      Cheese: {
        modify: 0.7,
        options: {
          7: { p: 0.65 },
          8: { p: 0.5 },
        },
      },
      'Popular Add-ons': {
        modify: 0.3,
        options: {
          9: { p: 0.5 },
          10: { p: 0.3 },
          11: { p: 0.3 },
          12: { p: 0.15 },
        },
      },
      Toppings: {
        modify: 0.5,
        // POOQ ids when generator picks amount: 13→1,2,3  14→4,5,6  15→7,8,9  16→10,11,12  17→13,14,15
        options: {
          13: { p: 0.7 },
          14: { p: 0.7 },
          15: { p: 0.7 },
          16: { p: 0.7 },
          17: { p: 0.7 },
        },
      },
      Sauces: {
        modify: 0.5,
        // POOQ ids: 18→16–18  19→19–21  20→22–24  21→25–27
        options: {
          18: { p: 0.7 },
          19: { p: 0.6 },
          20: { p: 0.7 },
          21: { p: 0.6 },
        },
      },
    },
    Whatthechicken: {
      Bread: {
        modify: 0.5,
        options: {
          66: 0.5,
          67: 0.2,
          68: 0.2,
          69: 0.1,
        },
      },
      Chicken: {
        modify: 0.5,
        options: {
          70: 1.0,
        },
      },
      Cheese: {
        modify: 0.4,
        options: {
          71: { p: 0.5 },
          72: { p: 0.65 },
        },
      },
      'Popular Add-ons': {
        modify: 0.3,
        options: {
          73: { p: 0.4 },
          74: { p: 0.2 },
          75: { p: 0.2 },
          76: { p: 0.1 },
        },
      },
      Toppings: {
        modify: 0.5,
        // POOQ: 77→82–84  78→85–86  79→87–89  80→90–92  81→93–95
        options: {
          77: { p: 0.7 },
          78: { p: 0.7 },
          79: { p: 0.7 },
          80: { p: 0.7 },
          81: { p: 0.7 },
        },
      },
      Sauces: {
        modify: 0.5,
        // POOQ: 82→96–98  83→99–101  84→102–104  85→105–107
        options: {
          82: { p: 0.5 },
          83: { p: 0.3 },
          84: { p: 0.6 },
          85: { p: 0.1 },
        },
      },
    },
    'Whatthechicken Strips 3 pcs': {
      Bread: {
        modify: 0.5,
        options: {
          86: 1.0,
        },
      },
      'Popular Add-ons': {
        modify: 0.7,
        options: {
          87: 1.0,
        },
      },
      'Dipping Sauce': {
        modify: 0.7,
        options: {
          88: 0.4,
          89: 0.2,
          90: 0.2,
          91: 0.15,
          92: 0.05,
        },
      },
    },
    'Bacon & Cheese Whattheburger': {
      Bread: {
        modify: 0.5,
        options: {
          22: 0.2,
          23: 0.2,
          24: 0.25,
          25: 0.25,
          26: 0.1,
        },
      },
      Beef: {
        modify: 0.5,
        options: {
          27: 1.0,
        },
      },
      'Included Cheese': {
        modify: 0.3,
        options: {
          28: 0.2,
          29: 0.6,
          30: 0.2,
        },
      },
      'Popular Add-ons': {
        modify: 0.3,
        options: {
          31: { p: 0.2 },
          32: { p: 0.2 },
          33: { p: 0.5 },
          34: { p: 0.3 },
          35: { p: 0.3 },
          36: { p: 0.15 },
          37: { p: 0.3 },
        },
      },
      Toppings: {
        modify: 0.5,
        // POOQ: 38→28–30  39→31–33  40→34–36  41→37–39  42→40–42
        options: {
          38: { p: 0.7 },
          39: { p: 0.7 },
          40: { p: 0.7 },
          41: { p: 0.7 },
          42: { p: 0.6 },
        },
      },
      Sauces: {
        modify: 0.5,
        // POOQ: 43→43–45  44→46–48  45→49–51  46→52–54
        options: {
          43: { p: 0.3 },
          44: { p: 0.4 },
          45: { p: 0.2 },
          46: { p: 0.4 },
        },
      },
    },
    'French Fries': {
      'French Fries': {
        modify: 0.5,
        options: {
          154: 1.0,
        },
        // POOQ size on option 154: Small (179), Medium (180, default), Large (181) — weights.pooqSizes
      },
      'Dipping Sauces': {
        modify: 0.5,
        options: {
          155: { p: 0.6 },
          156: { p: 0.6 },
          157: { p: 0.4 },
          158: { p: 0.3 },
        },
      },
    },
    'Patty Melt': {
      Bread: {
        modify: 0.3,
        options: {
          130: 0.4,
          131: 0.15,
          132: 0.3,
          133: 0.1,
          134: 0.05,
        },
      },
      Beef: {
        modify: 0.5,
        options: {
          135: 1.0,
        },
      },
      'Included Cheese': {
        modify: 0.3,
        options: {
          136: 0.6,
          137: 0.2,
          138: 0.2,
        },
      },
      'Popular Add-ons': {
        modify: 0.3,
        options: {
          139: { p: 0.8 },
          140: { p: 0.8 },
          141: { p: 0.5 },
          142: { p: 0.7 },
          143: { p: 0.7 },
          144: { p: 0.85 },
        },
      },
      Toppings: {
        modify: 0.5,
        // POOQ: 145→152–154  146→155–157  147→158–160  148→161–163  149→164–166
        options: {
          145: { p: 0.8 },
          146: { p: 0.8 },
          147: { p: 0.8 },
          148: { p: 0.7 },
          149: { p: 0.6 },
        },
      },
      Sauces: {
        modify: 0.5,
        // POOQ: 150→167–169  151→170–172  152→173–175  153→176–178
        options: {
          150: { p: 0.6 },
          151: { p: 0.8 },
          152: { p: 0.7 },
          153: { p: 0.9 },
        },
      },
    },
  },

  /** UNCOUNTABLE amount labels on burgers (Easy / Regular / Extra). */
  pooq: {
    Easy: 0.15,
    Regular: 0.7,
    Extra: 0.15,
  },

  /** UNCOUNTABLE size labels (e.g. French Fries). Merged with pooq; only matching quantityTypes apply. */
  pooqSizes: {
    Small: 0.15,
    Medium: 0.7,
    Large: 0.15,
  },
};

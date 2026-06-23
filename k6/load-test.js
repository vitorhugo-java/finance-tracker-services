/**
 * K6 Load Test — Transaction Service via API Gateway
 *
 * Prerequisites:
 *   - k6 installed (https://k6.io/docs/get-started/installation/)
 *   - Stack running: docker compose up -d
 *   - Keycloak realm imported with k6-test-client (directAccessGrantsEnabled=true)
 *
 * Run:
 *   k6 run k6/load-test.js
 *
 * Run with custom env:
 *   k6 run --env BASE_URL=http://localhost:8080 k6/load-test.js
 *
 * Run specific scenario:
 *   k6 run --env SCENARIO=smoke k6/load-test.js
 */

import http from "k6/http";
import { check, sleep, fail } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";
import { uuidv4 } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";

// ─── Config ──────────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const REALM = "finance-tracker-realm";
const CLIENT_ID = "k6-test-client";
const CLIENT_SECRET = "k6-test-secret";
const USERNAME = "admin";
const PASSWORD = "admin123";

// Auth routed through the Gateway (/auth/** → keycloak:8080), so Keycloak port
// doesn't need to be publicly exposed.
const TOKEN_ENDPOINT = `${BASE_URL}/auth/realms/${REALM}/protocol/openid-connect/token`;
const TRANSACTIONS_URL = `${BASE_URL}/api/transactions/transactions`;

// ─── Custom Metrics ──────────────────────────────────────────────────────────

const createSuccessRate = new Rate("transaction_create_success_rate");
const listSuccessRate = new Rate("transaction_list_success_rate");
const idempotencyHitRate = new Rate("idempotency_cache_hit_rate");
const createDuration = new Trend("transaction_create_duration", true);
const listDuration = new Trend("transaction_list_duration", true);
const authDuration = new Trend("auth_token_duration", true);
const transactionsCreated = new Counter("transactions_created_total");
const transactionsListed = new Counter("transactions_listed_total");

// ─── Readiness ───────────────────────────────────────────────────────────────

// Keycloak's OIDC well-known endpoint — returns 200 once the realm is imported and ready.
// Goes through the gateway (/auth/** → keycloak:8080) so no extra port needed.
const KEYCLOAK_HEALTH_URL = `${BASE_URL}/auth/realms/${REALM}/.well-known/openid-configuration`;
const READINESS_TIMEOUT_S = 120;
const READINESS_POLL_S = 3;

/**
 * Blocks until Keycloak signals it is ready or times out.
 * Runs once before any VU starts.
 */
export function setup() {
  const deadline = Date.now() + READINESS_TIMEOUT_S * 1000;
  console.log(`[setup] Waiting for Keycloak at ${KEYCLOAK_HEALTH_URL} …`);

  while (Date.now() < deadline) {
    const res = http.get(KEYCLOAK_HEALTH_URL, { timeout: "5s", tags: { name: "keycloak_health" } });
    if (res.status === 200) {
      console.log("[setup] Keycloak is ready.");
      return;
    }
    console.warn(`[setup] Keycloak not ready yet (status=${res.status}), retrying in ${READINESS_POLL_S}s …`);
    sleep(READINESS_POLL_S);
  }

  fail(`[setup] Keycloak did not become ready within ${READINESS_TIMEOUT_S}s`);
}

// ─── Scenarios ───────────────────────────────────────────────────────────────

const ACTIVE_SCENARIO = __ENV.SCENARIO || "mixed";

const SCENARIOS = {
  /** Quick sanity check — 1 VU, 1 iteration */
  smoke: {
    smoke: {
      executor: "per-vu-iterations",
      vus: 1,
      iterations: 1,
      maxDuration: "30s",
    },
  },

  /** Ramp up to 20 VUs over 1 min, hold 3 min, ramp down */
  load: {
    create_load: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "1m", target: 10 },
        { duration: "3m", target: 20 },
        { duration: "1m", target: 0 },
      ],
      exec: "createTransactions",
      tags: { scenario: "create_load" },
    },
    list_load: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "1m", target: 5 },
        { duration: "3m", target: 10 },
        { duration: "1m", target: 0 },
      ],
      exec: "listTransactions",
      tags: { scenario: "list_load" },
    },
  },

  /** Spike: sudden burst of 50 VUs */
  spike: {
    spike: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 50 },
        { duration: "1m", target: 50 },
        { duration: "30s", target: 0 },
      ],
      exec: "createTransactions",
      tags: { scenario: "spike" },
    },
  },

  /** Soak: sustained medium load for 10 min */
  soak: {
    soak: {
      executor: "constant-vus",
      vus: 15,
      duration: "10m",
      exec: "mixedWorkload",
      tags: { scenario: "soak" },
    },
  },

  /** Mixed: creates + reads + idempotency simultaneously */
  mixed: {
    create_income: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 5 },
        { duration: "2m", target: 10 },
        { duration: "30s", target: 0 },
      ],
      exec: "createIncomeTransactions",
      tags: { scenario: "create_income" },
    },
    create_expense: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 5 },
        { duration: "2m", target: 10 },
        { duration: "30s", target: 0 },
      ],
      exec: "createExpenseTransactions",
      tags: { scenario: "create_expense" },
    },
    list_paginated: {
      executor: "constant-vus",
      vus: 5,
      duration: "3m",
      exec: "listTransactions",
      tags: { scenario: "list_paginated" },
    },
    idempotency_test: {
      executor: "per-vu-iterations",
      vus: 3,
      iterations: 5,
      exec: "testIdempotency",
      tags: { scenario: "idempotency" },
    },
  },
};

export const options = {
  scenarios: SCENARIOS[ACTIVE_SCENARIO],

  thresholds: {
    // 95% of create requests under 800ms
    transaction_create_duration: ["p(95)<800"],
    // 95% of list requests under 500ms
    transaction_list_duration: ["p(95)<500"],
    // 95% of auth calls under 1s
    auth_token_duration: ["p(95)<1000"],
    // Success rates
    transaction_create_success_rate: ["rate>0.95"],
    transaction_list_success_rate: ["rate>0.98"],
    // Overall HTTP error rate
    http_req_failed: ["rate<0.05"],
  },
};

// ─── Data Fixtures ────────────────────────────────────────────────────────────

const INCOME_CATEGORIES = ["WORK", "FREELANCE", "INVESTMENT", "RENTAL", "GIFT", "BONUS"];
const EXPENSE_CATEGORIES = ["FOOD", "TRANSPORT", "ENTERTAINMENT", "BILLS", "HEALTH", "EDUCATION", "SHOPPING", "SUBSCRIPTIONS"];

const INCOME_DESCRIPTIONS = [
  "Monthly salary",
  "Freelance project payment",
  "Stock dividend",
  "Rental income",
  "Consulting fee",
  "Year-end bonus",
  "Side project revenue",
  "Investment return",
];

const EXPENSE_DESCRIPTIONS = [
  "Supermarket groceries",
  "Uber ride",
  "Netflix subscription",
  "Electric bill",
  "Dentist appointment",
  "Online course",
  "New shoes",
  "Restaurant dinner",
  "Gym membership",
  "Pharmacy",
  "Fuel",
  "Internet bill",
];

function randomElement(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randomAmount(min, max) {
  return parseFloat((Math.random() * (max - min) + min).toFixed(2));
}

function randomDate() {
  // Random date within the last 90 days
  const daysAgo = Math.floor(Math.random() * 90);
  const date = new Date();
  date.setDate(date.getDate() - daysAgo);
  return date.toISOString();
}

function buildIncomePayload() {
  return {
    description: randomElement(INCOME_DESCRIPTIONS),
    amount: randomAmount(500, 15000),
    category: randomElement(INCOME_CATEGORIES),
    type: "INCOME",
    transactionDate: randomDate(),
  };
}

function buildExpensePayload() {
  return {
    description: randomElement(EXPENSE_DESCRIPTIONS),
    amount: randomAmount(5, 2000),
    category: randomElement(EXPENSE_CATEGORIES),
    type: "EXPENSE",
    transactionDate: randomDate(),
  };
}

// ─── Auth ─────────────────────────────────────────────────────────────────────

/**
 * Obtains a Bearer token via Resource Owner Password Credentials grant.
 * Called once per VU in setup or per-request depending on token lifetime.
 */
function getToken() {
  const start = Date.now();

  const payload = {
    grant_type: "password",
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET,
    username: USERNAME,
    password: PASSWORD,
    scope: "openid",
  };

  const res = http.post(TOKEN_ENDPOINT, payload, {
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    tags: { name: "keycloak_token" },
  });

  authDuration.add(Date.now() - start);

  const ok = check(res, {
    "auth: status 200": (r) => r.status === 200,
    "auth: has access_token": (r) => {
      try {
        return JSON.parse(r.body).access_token !== undefined;
      } catch {
        return false;
      }
    },
  });

  if (!ok) {
    console.error(`[auth] Token request failed — status=${res.status} body=${res.body}`);
    return null;
  }

  const body = JSON.parse(res.body);
  return {
    accessToken: body.access_token,
    expiresIn: body.expires_in, // seconds
    obtainedAt: Date.now(),
  };
}

// ─── Token Cache (per VU) ────────────────────────────────────────────────────

let _tokenCache = null;

function getOrRefreshToken() {
  const now = Date.now();
  // Refresh 30s before expiry
  if (
    !_tokenCache ||
    now >= _tokenCache.obtainedAt + (_tokenCache.expiresIn - 30) * 1000
  ) {
    _tokenCache = getToken();
  }
  return _tokenCache ? _tokenCache.accessToken : null;
}

// ─── Request Helpers ──────────────────────────────────────────────────────────

function authHeaders(token, extra = {}) {
  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
    ...extra,
  };
}

function createTransaction(token, payload) {
  const idempotencyKey = uuidv4();
  const start = Date.now();

  const res = http.post(TRANSACTIONS_URL, JSON.stringify(payload), {
    headers: authHeaders(token, { idempotencyKey }),
    tags: { name: "create_transaction" },
  });

  createDuration.add(Date.now() - start);

  const success = check(res, {
    "create: status 200 or 201": (r) => r.status === 200 || r.status === 201,
    "create: has transaction id": (r) => {
      try {
        return JSON.parse(r.body).id !== undefined;
      } catch {
        return false;
      }
    },
  });

  createSuccessRate.add(success);
  if (success) transactionsCreated.add(1);

  if (!success) {
    console.warn(`[create] Failed — status=${res.status} body=${res.body.substring(0, 200)}`);
  }

  return { res, idempotencyKey };
}

function listTransactionPage(token, page = 0, size = 20) {
  const start = Date.now();

  const res = http.get(`${TRANSACTIONS_URL}?page=${page}&size=${size}`, {
    headers: authHeaders(token),
    tags: { name: "list_transactions" },
  });

  listDuration.add(Date.now() - start);

  const success = check(res, {
    "list: status 200": (r) => r.status === 200,
    "list: has content array": (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body.content);
      } catch {
        return false;
      }
    },
    "list: has pagination info": (r) => {
      try {
        const body = JSON.parse(r.body);
        return (
          body.pageNumber !== undefined &&
          body.pageSize !== undefined &&
          body.totalElements !== undefined
        );
      } catch {
        return false;
      }
    },
  });

  listSuccessRate.add(success);
  if (success) transactionsListed.add(1);

  return res;
}

// ─── Scenario Executors ───────────────────────────────────────────────────────

/** Create only INCOME transactions */
export function createIncomeTransactions() {
  const token = getOrRefreshToken();
  if (!token) return;

  createTransaction(token, buildIncomePayload());
  sleep(Math.random() * 1 + 0.5); // 0.5–1.5s between requests
}

/** Create only EXPENSE transactions */
export function createExpenseTransactions() {
  const token = getOrRefreshToken();
  if (!token) return;

  createTransaction(token, buildExpensePayload());
  sleep(Math.random() * 1 + 0.5);
}

/** Create mixed transactions (random type) */
export function createTransactions() {
  const token = getOrRefreshToken();
  if (!token) return;

  const payload = Math.random() > 0.4 ? buildExpensePayload() : buildIncomePayload();
  createTransaction(token, payload);
  sleep(Math.random() * 1 + 0.3);
}

/** List transactions with random pagination */
export function listTransactions() {
  const token = getOrRefreshToken();
  if (!token) return;

  const page = Math.floor(Math.random() * 5); // pages 0–4
  const size = randomElement([10, 20, 50]);

  listTransactionPage(token, page, size);
  sleep(Math.random() * 2 + 1); // 1–3s between reads
}

/**
 * Idempotency test:
 * 1. Create a transaction and store the idempotency key.
 * 2. Repeat the same request — should return HTTP 200 (cached).
 */
export function testIdempotency() {
  const token = getOrRefreshToken();
  if (!token) return;

  const payload = buildIncomePayload();

  // First call — expect 201 Created
  const { res: firstRes, idempotencyKey } = createTransaction(token, payload);

  check(firstRes, {
    "idempotency: first call is 201": (r) => r.status === 201,
  });

  sleep(0.5);

  // Second call with the same key — expect 200 OK (cache hit)
  const start = Date.now();
  const secondRes = http.post(TRANSACTIONS_URL, JSON.stringify(payload), {
    headers: authHeaders(token, { idempotencyKey }),
    tags: { name: "idempotency_repeat" },
  });

  const cacheHit = check(secondRes, {
    "idempotency: repeat call is 200": (r) => r.status === 200,
    "idempotency: same transaction id": (r) => {
      try {
        const first = JSON.parse(firstRes.body);
        const second = JSON.parse(r.body);
        return first.id === second.id;
      } catch {
        return false;
      }
    },
  });

  idempotencyHitRate.add(cacheHit);
  sleep(1);
}

/** Mixed workload: ~60% creates, ~30% reads, ~10% idempotency checks */
export function mixedWorkload() {
  const token = getOrRefreshToken();
  if (!token) return;

  const roll = Math.random();

  if (roll < 0.60) {
    const payload = Math.random() > 0.4 ? buildExpensePayload() : buildIncomePayload();
    createTransaction(token, payload);
    sleep(Math.random() * 1 + 0.5);
  } else if (roll < 0.90) {
    const page = Math.floor(Math.random() * 3);
    listTransactionPage(token, page);
    sleep(Math.random() * 2 + 1);
  } else {
    testIdempotency();
  }
}

// ─── Default function (used for smoke/single scenario runs) ──────────────────

export default function () {
  mixedWorkload();
}

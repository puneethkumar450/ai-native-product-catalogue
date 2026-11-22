import assert from "node:assert/strict";
import { spawn } from "node:child_process";
import { once } from "node:events";
import { createServer } from "node:http";
import { after, before, test } from "node:test";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const backendDirectory = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const products = [
  { id: 1, title: "Phone Case", price: 12.5, description: "Protective case", category: "electronics", image: "https://images.test/1.png", rating: { rate: 4.2, count: 10 } },
  { id: 2, title: "Wireless Phone", price: 89, description: "Compact phone", category: "electronics", image: "https://images.test/2.png", rating: { rate: 4.8, count: 20 } },
  { id: 3, title: "Gold Bracelet", price: 24, description: "A simple bracelet", category: "jewelery", image: "https://images.test/3.png", rating: { rate: 3.9, count: 8 } },
  { id: 4, title: "Canvas Bag", price: 31, description: "Phone-sized canvas bag", category: "women's clothing", image: "https://images.test/4.png", rating: { rate: 4.1, count: 5 } },
  { id: 5, title: "Wool Jacket", price: 64, description: "Warm winter jacket", category: "men's clothing", image: "https://images.test/5.png", rating: { rate: 4.4, count: 7 } },
  { id: 6, title: "USB Cable", price: 8, description: "Fast charging cable", category: "electronics", image: "https://images.test/6.png", rating: { rate: 3.7, count: 15 } },
];

let sourceServer;
let backendProcess;
let apiBaseUrl;
let sourceFailureStatus = null;

function listen(server) {
  return new Promise((resolveListen, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", () => {
      server.off("error", reject);
      resolveListen(server.address().port);
    });
  });
}

function sendJson(response, status, body) {
  response.writeHead(status, { "content-type": "application/json" });
  response.end(JSON.stringify(body));
}

before(async () => {
  sourceServer = createServer((request, response) => {
    const path = new URL(request.url ?? "/", "http://localhost").pathname;
    if (sourceFailureStatus !== null) return sendJson(response, sourceFailureStatus, { error: "Source unavailable" });
    if (path === "/products/categories") return sendJson(response, 200, ["electronics", "jewelery", "men's clothing", "women's clothing"]);
    if (path === "/products") return sendJson(response, 200, products);
    const productMatch = path.match(/^\/products\/(\d+)$/);
    if (productMatch) {
      const product = products.find((item) => item.id === Number(productMatch[1]));
      return sendJson(response, product ? 200 : 404, product ?? {});
    }
    return sendJson(response, 404, { error: "Not found" });
  });
  const sourcePort = await listen(sourceServer);
  const apiPort = await new Promise((resolvePort, reject) => {
    const probe = createServer();
    probe.once("error", reject);
    probe.listen(0, "127.0.0.1", () => {
      const port = probe.address().port;
      probe.close((error) => error ? reject(error) : resolvePort(port));
    });
  });
  apiBaseUrl = `http://127.0.0.1:${apiPort}`;
  backendProcess = spawn(process.execPath, ["dist/server.js"], {
    cwd: backendDirectory,
    env: {
      ...process.env,
      PORT: String(apiPort),
      FAKE_STORE_API_URL: `http://127.0.0.1:${sourcePort}`,
      SMTP_HOST: "",
    },
    stdio: "ignore",
  });

  for (let attempt = 0; attempt < 50; attempt += 1) {
    try {
      const response = await fetch(`${apiBaseUrl}/health`);
      if (response.ok) return;
    } catch {
      // The child process may need a moment to bind its port.
    }
    await new Promise((resolveWait) => setTimeout(resolveWait, 100));
  }
  throw new Error("API server did not start");
});

after(async () => {
  backendProcess?.kill("SIGTERM");
  await Promise.all([
    sourceServer ? new Promise((resolveClose) => sourceServer.close(resolveClose)) : Promise.resolve(),
    backendProcess ? once(backendProcess, "exit").catch(() => {}) : Promise.resolve(),
  ]);
});

async function apiRequest(path, options) {
  const response = await fetch(`${apiBaseUrl}${path}`, options);
  return { status: response.status, body: await response.json() };
}

test("health check returns ok", async () => {
  const result = await apiRequest("/health");
  assert.equal(result.status, 200);
  assert.deepEqual(result.body, { status: "ok" });
});

test("featured endpoint returns the five highest-rated products in rating order", async () => {
  const result = await apiRequest("/api/products/featured");
  assert.equal(result.status, 200);
  assert.equal(result.body.length, 5);
  assert.deepEqual(result.body.map((product) => product.id), [2, 5, 1, 4, 3]);
  assert.deepEqual(
    result.body.map((product) => product.rating.rate),
    [4.8, 4.4, 4.2, 4.1, 3.9],
  );
});

test("categories endpoint returns available categories", async () => {
  const result = await apiRequest("/api/categories");
  assert.equal(result.status, 200);
  assert.ok(result.body.includes("electronics"));
});

test("category products are rating sorted and include the rest of the category", async () => {
  const result = await apiRequest("/api/categories/electronics/products");
  assert.equal(result.status, 200);
  assert.deepEqual(result.body.map((product) => product.id), [2, 1, 6]);
});

test("search matches product text", async () => {
  const result = await apiRequest("/api/products?q=phone");
  assert.equal(result.status, 200);
  assert.deepEqual(result.body.map((product) => product.id).sort(), [1, 2, 4]);
});

test("product endpoint returns details and uses 404 for an unknown product", async () => {
  const found = await apiRequest("/api/products/3");
  assert.equal(found.status, 200);
  assert.equal(found.body.title, "Gold Bracelet");

  const missing = await apiRequest("/api/products/999");
  assert.equal(missing.status, 404);
});

test("unknown routes and malformed category paths return client errors", async () => {
  const missingRoute = await apiRequest("/api/not-a-route");
  assert.equal(missingRoute.status, 404);

  const malformedCategory = await apiRequest("/api/categories/%ZZ/products");
  assert.equal(malformedCategory.status, 400);
});

test("product source failures return a gateway error", async () => {
  sourceFailureStatus = 503;
  try {
    const result = await apiRequest("/api/products/featured");
    assert.equal(result.status, 502);
    assert.match(result.body.error, /temporarily unavailable/i);
  } finally {
    sourceFailureStatus = null;
  }
});

test("enquiry rejects invalid JSON shapes, content types, and oversized bodies", async () => {
  const malformed = await apiRequest("/api/enquiries", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: "{",
  });
  assert.equal(malformed.status, 400);

  const notObject = await apiRequest("/api/enquiries", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: "null",
  });
  assert.equal(notObject.status, 400);

  const wrongType = await apiRequest("/api/enquiries", {
    method: "POST",
    headers: { "content-type": "text/plain" },
    body: "{}",
  });
  assert.equal(wrongType.status, 415);

  const tooLarge = await apiRequest("/api/enquiries", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: "x".repeat(16_385),
  });
  assert.equal(tooLarge.status, 413);
});

test("enquiry validates IDs, name, email, and optional message limits", async () => {
  const validContact = { name: "Ada", email: "ada@example.com", message: "" };
  const invalidPayloads = [
    { ...validContact, productId: "1" },
    { ...validContact, productId: 0 },
    { ...validContact, productId: -1 },
    { ...validContact, productId: 1.5 },
    { ...validContact, productId: 1, name: "   " },
    { ...validContact, productId: 1, email: "not-an-email" },
    { ...validContact, productId: 1, email: "a".repeat(250) + "@example.com" },
    { ...validContact, productId: 1, message: 123 },
    { ...validContact, productId: 1, message: "x".repeat(2001) },
    { ...validContact, productId: 1, name: "x".repeat(121) },
  ];

  for (const payload of invalidPayloads) {
    const result = await apiRequest("/api/enquiries", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(payload),
    });
    assert.equal(result.status, 400, `Expected rejection for ${JSON.stringify(payload).slice(0, 80)}`);
  }
});

test("enquiry validates contact details and returns a thank-you response", async () => {
  const invalid = await apiRequest("/api/enquiries", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ productId: 1, name: "Ada", email: "not-an-email" }),
  });
  assert.equal(invalid.status, 400);

  const accepted = await apiRequest("/api/enquiries", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ productId: 1, name: "Ada", email: "ada@example.com", message: "x".repeat(2000) }),
  });
  assert.equal(accepted.status, 201);
  assert.match(accepted.body.message, /Thank you/i);

  const optionalMessage = await apiRequest("/api/enquiries", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ productId: 1, name: "Ada", email: "ada@example.com" }),
  });
  assert.equal(optionalMessage.status, 201);
});

test("enquiry returns 404 when the product does not exist", async () => {
  const result = await apiRequest("/api/enquiries", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ productId: 999, name: "Ada", email: "ada@example.com" }),
  });
  assert.equal(result.status, 404);
});

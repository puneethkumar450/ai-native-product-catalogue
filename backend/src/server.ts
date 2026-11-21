import { createServer, type IncomingMessage, type ServerResponse } from "node:http";
import {
  getCategoryProducts,
  getFeaturedProducts,
  productSource,
  searchProducts,
} from "./products.js";

const port = Number(process.env.PORT ?? 3000);

function send(response: ServerResponse, status: number, body: unknown): void {
  response.writeHead(status, { "content-type": "application/json; charset=utf-8" });
  response.end(JSON.stringify(body));
}

async function readJson(request: IncomingMessage): Promise<unknown> {
  let body = "";
  for await (const chunk of request) {
    body += chunk;
    if (body.length > 16_384) throw new Error("Request body is too large");
  }
  return JSON.parse(body);
}

const server = createServer(async (request, response) => {
  const url = new URL(request.url ?? "/", `http://${request.headers.host ?? "localhost"}`);
  const method = request.method ?? "GET";

  try {
    if (method === "GET" && url.pathname === "/health") {
      return send(response, 200, { status: "ok" });
    }

    if (method === "GET" && url.pathname === "/api/products/featured") {
      return send(response, 200, await getFeaturedProducts());
    }

    if (method === "GET" && url.pathname === "/api/categories") {
      return send(response, 200, await productSource.getCategories());
    }

    const categoryMatch = url.pathname.match(/^\/api\/categories\/([^/]+)\/products$/);
    if (method === "GET" && categoryMatch) {
      return send(response, 200, await getCategoryProducts(decodeURIComponent(categoryMatch[1])));
    }

    if (method === "GET" && url.pathname === "/api/products") {
      const query = url.searchParams.get("q");
      return send(response, 200, query ? await searchProducts(query) : await productSource.getAll());
    }

    const productMatch = url.pathname.match(/^\/api\/products\/(\d+)$/);
    if (method === "GET" && productMatch) {
      const product = await productSource.getById(Number(productMatch[1]));
      if (!product) return send(response, 404, { error: "Product not found" });
      return send(response, 200, product);
    }

    if (method === "POST" && url.pathname === "/api/enquiries") {
      const payload = await readJson(request) as Record<string, unknown>;
      const productId = Number(payload.productId);
      const name = typeof payload.name === "string" ? payload.name.trim() : "";
      const email = typeof payload.email === "string" ? payload.email.trim() : "";
      const message = typeof payload.message === "string" ? payload.message.trim() : "";

      if (!Number.isInteger(productId) || !name || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        return send(response, 400, { error: "Provide a productId, name, and valid email address" });
      }
      if (message.length > 2000) return send(response, 400, { error: "Message must be 2000 characters or fewer" });

      const product = await productSource.getById(productId);
      if (!product) return send(response, 404, { error: "Product not found" });
      console.info("Enquiry received (local email capture):", JSON.stringify({
        productId,
        productTitle: product.title,
        name,
        email,
        message,
        receivedAt: new Date().toISOString(),
      }));
      return send(response, 201, { message: "Thank you. Your enquiry has been received." });
    }

    return send(response, 404, { error: "Route not found" });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unexpected server error";
    const status = message === "Unexpected end of JSON input" || message === "Request body is too large" ? 400 : 502;
    console.error(message);
    return send(response, status, { error: status === 400 ? message : "Catalogue service is temporarily unavailable" });
  }
});

server.listen(port, "0.0.0.0", () => {
  console.info(`Catalogue API listening on http://0.0.0.0:${port}`);
});

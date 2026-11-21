import { createServer, type IncomingMessage, type ServerResponse } from "node:http";
import {
  getCategoryProducts,
  getFeaturedProducts,
  productSource,
  searchProducts,
} from "./products.js";
import { sendProductEnquiry } from "./mailer.js";

const port = Number(process.env.PORT ?? 3000);

class ApiInputError extends Error {
  constructor(message: string, readonly status: number = 400) {
    super(message);
  }
}

function send(response: ServerResponse, status: number, body: unknown): void {
  response.writeHead(status, { "content-type": "application/json; charset=utf-8" });
  response.end(JSON.stringify(body));
}

async function readJson(request: IncomingMessage): Promise<unknown> {
  let body = "";
  for await (const chunk of request) {
    body += chunk;
    if (body.length > 16_384) throw new ApiInputError("Request body is too large", 413);
  }
  try {
    return JSON.parse(body);
  } catch {
    throw new ApiInputError("Request body must contain valid JSON");
  }
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
      const contentType = request.headers["content-type"]?.split(";", 1)[0].trim().toLowerCase();
      if (contentType !== "application/json") {
        throw new ApiInputError("Content-Type must be application/json", 415);
      }
      const payload = await readJson(request);
      if (payload === null || typeof payload !== "object" || Array.isArray(payload)) {
        throw new ApiInputError("Request body must be a JSON object");
      }
      const fields = payload as Record<string, unknown>;
      const productId = fields.productId;
      const name = typeof fields.name === "string" ? fields.name.trim() : "";
      const email = typeof fields.email === "string" ? fields.email.trim() : "";
      const message = typeof fields.message === "string" ? fields.message.trim() : "";

      if (typeof productId !== "number" || !Number.isSafeInteger(productId) || productId <= 0 || !name || email.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        return send(response, 400, { error: "Provide a productId, name, and valid email address" });
      }
      if (fields.message !== undefined && typeof fields.message !== "string") {
        return send(response, 400, { error: "Message must be text" });
      }
      if (name.length > 120) return send(response, 400, { error: "Name must be 120 characters or fewer" });
      if (message.length > 2000) return send(response, 400, { error: "Message must be 2000 characters or fewer" });

      const product = await productSource.getById(productId);
      if (!product) return send(response, 404, { error: "Product not found" });
      await sendProductEnquiry({
        productId,
        productTitle: product.title,
        name,
        email,
        message,
        receivedAt: new Date().toISOString(),
      });
      return send(response, 201, { message: "Thank you. Your enquiry has been received." });
    }

    return send(response, 404, { error: "Route not found" });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unexpected server error";
    const status = error instanceof ApiInputError ? error.status : error instanceof URIError ? 400 : 502;
    if (status >= 500) console.error(message);
    const errorMessage = error instanceof ApiInputError
      ? message
      : error instanceof URIError
        ? "Category path contains invalid encoding"
        : "Catalogue service is temporarily unavailable";
    return send(response, status, { error: errorMessage });
  }
});

server.listen(port, "0.0.0.0", () => {
  console.info(`Catalogue API listening on http://0.0.0.0:${port}`);
});

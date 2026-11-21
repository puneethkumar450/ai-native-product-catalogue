export interface Product {
  id: number;
  title: string;
  price: number;
  description: string;
  category: string;
  image: string;
  rating?: { rate: number; count: number };
}

const featuredProductIds = new Set([1, 2, 3, 4, 5]);
const apiBaseUrl = (process.env.FAKE_STORE_API_URL ?? "https://fakestoreapi.com").replace(/\/$/, "");

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, { signal: AbortSignal.timeout(8000) });
  if (!response.ok) throw new Error(`Product source returned HTTP ${response.status}`);
  return response.json() as Promise<T>;
}

// All external catalogue access stays behind this module so the source can be replaced later.
export const productSource = {
  getAll: () => request<Product[]>("/products"),
  async getById(id: number): Promise<Product | null> {
    const response = await fetch(`${apiBaseUrl}/products/${id}`, { signal: AbortSignal.timeout(8000) });
    if (response.status === 404) return null;
    if (!response.ok) throw new Error(`Product source returned HTTP ${response.status}`);
    const product = await response.json() as Product;
    return product?.id ? product : null;
  },
  getCategories: () => request<string[]>("/products/categories"),
};

export async function getFeaturedProducts(): Promise<Product[]> {
  const products = await productSource.getAll();
  return products.filter((product) => featuredProductIds.has(product.id));
}

export async function getCategoryProducts(category: string): Promise<Product[]> {
  const products = await productSource.getAll();
  return products
    .filter((product) => product.category.toLowerCase() === category.toLowerCase())
    .sort((a, b) => (b.rating?.rate ?? 0) - (a.rating?.rate ?? 0));
}

export async function searchProducts(term: string): Promise<Product[]> {
  const query = term.trim().toLowerCase();
  if (!query) return [];
  const products = await productSource.getAll();
  return products.filter((product) =>
    [product.title, product.description, product.category].some((value) => value.toLowerCase().includes(query)),
  );
}

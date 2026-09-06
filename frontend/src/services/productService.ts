import type { Product } from '../types/product';

const API_BASE_URL = 'http://localhost:8080/api/products';

export const productService = {
  async getAll(): Promise<Product[]> {
    const response = await fetch(API_BASE_URL, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
    if (!response.ok) {
      throw new Error(`Error al obtener productos: ${response.statusText}`);
    }
    return response.json();
  },

  async create(product: Product): Promise<Product> {
    const response = await fetch(API_BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(product),
    });
    if (!response.ok) {
      throw new Error(`Error al crear producto: ${response.statusText}`);
    }
    return response.json();
  },

  async delete(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error(`Error al eliminar producto: ${response.statusText}`);
    }
  },
};

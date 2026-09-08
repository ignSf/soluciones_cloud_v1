import type { Product } from '../types/product';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://soluciones-cloud-v1.onrender.com/api/products';

export const productService = {
  /**
   * Obtener productos (Endpoint público en backend)
   */
  async getAll(): Promise<Product[]> {
    const response = await fetch(API_BASE_URL, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
    if (!response.ok) {
      throw new Error(`Error al obtener productos: ${response.status} ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * Crear producto (Endpoint protegido: requiere Bearer token)
   */
  async create(product: Product, token?: string): Promise<Product> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(API_BASE_URL, {
      method: 'POST',
      headers,
      body: JSON.stringify(product),
    });

    if (response.status === 401) {
      const errData = await response.json().catch(() => ({}));
      throw new Error(errData.error || '401 Unauthorized: Debe iniciar sesión con AWS Cognito para crear productos.');
    }

    if (!response.ok) {
      throw new Error(`Error al crear producto: ${response.status} ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * Eliminar producto (Endpoint protegido: requiere Bearer token)
   */
  async delete(id: number, token?: string): Promise<void> {
    const headers: Record<string, string> = {};

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE',
      headers,
    });

    if (response.status === 401) {
      throw new Error('401 Unauthorized: Debe iniciar sesión con AWS Cognito para eliminar productos.');
    }

    if (!response.ok) {
      throw new Error(`Error al eliminar producto: ${response.status} ${response.statusText}`);
    }
  },
};

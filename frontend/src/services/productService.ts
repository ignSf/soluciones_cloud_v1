import type { Product } from '../types/product';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/products';

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

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado: Se requiere una sesión válida de AWS Cognito.');
      }
      if (response.status === 400) {
        try {
          const errData = await response.json();
          if (errData.fieldErrors) {
            const details = Object.entries(errData.fieldErrors)
              .map(([field, msg]) => `${field}: ${msg}`)
              .join(', ');
            throw new Error(`Datos inválidos: ${details}`);
          }
        } catch (e: any) {
          if (e.message && e.message.startsWith('Datos inválidos:')) throw e;
        }
      }
      throw new Error(`Error al crear producto: ${response.statusText}`);
    }
    return response.json();
  },

  async delete(id: number, token?: string): Promise<void> {
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE',
      headers,
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado: Se requiere una sesión válida de AWS Cognito.');
      }
      if (response.status === 404) {
        throw new Error('El producto no fue encontrado en el catálogo.');
      }
      throw new Error(`Error al eliminar producto: ${response.statusText}`);
    }
  },
};

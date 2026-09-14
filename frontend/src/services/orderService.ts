import type { Order } from '../types/order';

const DEFAULT_ORDERS_URL = import.meta.env.VITE_ORDERS_API_URL ||
  (import.meta.env.VITE_API_URL 
    ? import.meta.env.VITE_API_URL.replace(/\/api\/products\/?$/, '/api/orders') 
    : 'https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod/api/orders');

export const orderService = {
  /**
   * Obtener pedidos del usuario autenticado
   */
  async getMyOrders(token?: string): Promise<Order[]> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${DEFAULT_ORDERS_URL}/my-orders`, {
      headers,
    });
    if (!response.ok) {
      throw new Error(`Error al obtener pedidos: ${response.status} ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * Crear un nuevo pedido con el token del usuario conectado
   */
  async create(order: Order, token?: string): Promise<Order> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(DEFAULT_ORDERS_URL, {
      method: 'POST',
      headers,
      body: JSON.stringify(order),
    });

    if (response.status === 401 || response.status === 403) {
      throw new Error('Debe iniciar sesión para confirmar su pedido.');
    }
    if (!response.ok) {
      throw new Error(`Error al procesar el pedido: ${response.status} ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * Obtener todas las órdenes (vista administrativa)
   */
  async getAll(token?: string): Promise<Order[]> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(DEFAULT_ORDERS_URL, {
      headers,
    });
    if (!response.ok) {
      throw new Error(`Error al consultar todas las órdenes: ${response.status}`);
    }
    return response.json();
  },
};

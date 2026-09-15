import type { Order } from '../types/order';

const RENDER_ORDERS_URL = 'https://soluciones-cloud-v1.onrender.com/api/orders';

// Si VITE_ORDERS_API_URL está definido, usarlo.
// Si VITE_API_URL está definido y NO es de AWS API Gateway (que no tiene /api/orders configurado), reemplazarlo.
// En cualquier otro caso, apuntar directamente al backend en Render.
const DEFAULT_ORDERS_URL = import.meta.env.VITE_ORDERS_API_URL ||
  (import.meta.env.VITE_API_URL && !import.meta.env.VITE_API_URL.includes('execute-api')
    ? import.meta.env.VITE_API_URL.replace(/\/api\/products\/?$/, '/api/orders') 
    : RENDER_ORDERS_URL);

/**
 * Función auxiliar con reintento automático hacia Render en caso de fallo de red (CORS o Gateway no mapeado)
 */
async function fetchOrdersWithFallback(endpoint: string, options: RequestInit): Promise<Response> {
  const primaryUrl = `${DEFAULT_ORDERS_URL}${endpoint}`;
  try {
    const res = await fetch(primaryUrl, options);
    // Si la pasarela devuelve 403 MissingAuthenticationTokenException o 404
    if (res.status === 403 || res.status === 404) {
      if (primaryUrl !== `${RENDER_ORDERS_URL}${endpoint}`) {
        console.warn(`[OrderService] Fallo en endpoint primario (${res.status}). Reintentando contra Render...`);
        return await fetch(`${RENDER_ORDERS_URL}${endpoint}`, options);
      }
    }
    return res;
  } catch (err) {
    // Si ocurre un error de red (Failed to fetch por CORS en API Gateway)
    if (primaryUrl !== `${RENDER_ORDERS_URL}${endpoint}`) {
      console.warn('[OrderService] Error de red en URL primaria. Redirigiendo petición directamente a Render...');
      return await fetch(`${RENDER_ORDERS_URL}${endpoint}`, options);
    }
    throw err;
  }
}

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

    const response = await fetchOrdersWithFallback('/my-orders', {
      headers,
    });

    if (response.status === 401 || response.status === 403) {
      throw new Error('Debe iniciar sesión para consultar sus pedidos.');
    }

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

    const response = await fetchOrdersWithFallback('', {
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

    const response = await fetchOrdersWithFallback('', {
      headers,
    });
    if (!response.ok) {
      throw new Error(`Error al consultar todas las órdenes: ${response.status}`);
    }
    return response.json();
  },
};

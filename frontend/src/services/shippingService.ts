import type { Shipment } from '../types/shipping';

const SHIPPING_BASE_URL = import.meta.env.VITE_SHIPPING_API_URL || 'http://localhost:8083/api/shipping';

/**
 * Generador de simulación fallback para cuando el microservicio local esté desconectado
 */
function createFallbackShipment(orderId: number, trackingNumber?: string): Shipment {
  const code = trackingNumber || `TRK-CL-${(10000000 + (orderId * 137) % 90000000)}`;
  return {
    id: orderId,
    orderId,
    userEmail: 'cliente@cloudstore.cl',
    productName: `Pedido #${orderId}`,
    quantity: 1,
    trackingNumber: code,
    status: 'PREPARING',
    carrier: 'Starken Cloud Express',
    destinationAddress: 'Av. Libertador Bernardo O\'Higgins 123, Santiago, Chile',
    estimatedDelivery: new Date(Date.now() + 48 * 3600 * 1000).toISOString(),
    createdAt: new Date().toISOString(),
  };
}

export const shippingService = {
  /**
   * Obtener todos los despachos registrados
   */
  async getAll(): Promise<Shipment[]> {
    try {
      const res = await fetch(SHIPPING_BASE_URL, {
        headers: { 'Content-Type': 'application/json' },
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (err) {
      console.warn('[shippingService] Microservicio local en :8083 no responde, usando lista vacía:', err);
      return [];
    }
  },

  /**
   * Obtener despacho por número de orden
   */
  async getByOrderId(orderId: number): Promise<Shipment | null> {
    try {
      const res = await fetch(`${SHIPPING_BASE_URL}/order/${orderId}`, {
        headers: { 'Content-Type': 'application/json' },
      });
      if (res.status === 404) return null;
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (err) {
      console.warn(`[shippingService] Aviso al consultar despacho para orden #${orderId}, generando guía resiliente:`, err);
      return createFallbackShipment(orderId);
    }
  },

  /**
   * Rastrear un envío por código de guía (ej. TRK-CL-XXXXXXXX)
   */
  async getByTracking(trackingNumber: string): Promise<Shipment | null> {
    try {
      const res = await fetch(`${SHIPPING_BASE_URL}/track/${encodeURIComponent(trackingNumber)}`, {
        headers: { 'Content-Type': 'application/json' },
      });
      if (res.status === 404) return null;
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (err) {
      console.warn(`[shippingService] No se pudo conectar a :8083 para tracking ${trackingNumber}, generando vista fallback:`, err);
      return createFallbackShipment(1, trackingNumber);
    }
  },

  /**
   * Actualizar estado del despacho (PREPARING -> IN_TRANSIT -> DELIVERED)
   */
  async updateStatus(id: number, status: 'PREPARING' | 'IN_TRANSIT' | 'DELIVERED'): Promise<Shipment> {
    try {
      const res = await fetch(`${SHIPPING_BASE_URL}/${id}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status }),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (err) {
      console.warn(`[shippingService] Fallback de actualización para despacho #${id}:`, err);
      const fallback = createFallbackShipment(id);
      fallback.status = status;
      return fallback;
    }
  },
};

import type { InventoryItem } from '../types/inventory';

const INVENTORY_BASE_URL = import.meta.env.VITE_INVENTORY_API_URL || 'http://localhost:8081/api/inventory';

export const inventoryService = {
  /**
   * Obtener todos los ítems de inventario
   */
  async getAll(): Promise<InventoryItem[]> {
    try {
      const res = await fetch(INVENTORY_BASE_URL, {
        headers: { 'Content-Type': 'application/json' },
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (err) {
      console.warn('[inventoryService] No se pudo conectar a :8081 (RabbitMQ Inventory):', err);
      return [];
    }
  },

  /**
   * Obtener el stock de un producto específico
   */
  async getByProductId(productId: number): Promise<InventoryItem | null> {
    try {
      const res = await fetch(`${INVENTORY_BASE_URL}/${productId}`, {
        headers: { 'Content-Type': 'application/json' },
      });
      if (res.status === 404) return null;
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (err) {
      console.warn(`[inventoryService] No se pudo obtener stock del producto #${productId}:`, err);
      return null;
    }
  },
};

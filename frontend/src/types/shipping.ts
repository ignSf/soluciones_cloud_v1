export interface Shipment {
  id: number;
  orderId: number;
  userEmail?: string;
  productId?: number;
  productName?: string;
  quantity?: number;
  trackingNumber: string;
  status: 'PREPARING' | 'IN_TRANSIT' | 'DELIVERED' | string;
  carrier?: string;
  destinationAddress?: string;
  estimatedDelivery?: string;
  createdAt?: string;
}

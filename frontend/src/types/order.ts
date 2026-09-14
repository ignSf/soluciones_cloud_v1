export interface Order {
  id?: number;
  userEmail?: string;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalAmount?: number;
  status?: string;
  createdAt?: string;
}

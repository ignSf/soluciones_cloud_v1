import React from 'react';
import type { Product } from '../types/product';
import { ProductCard } from './ProductCard';

interface ProductListProps {
  products: Product[];
  isLoading: boolean;
  onDelete: (id: number) => void;
}

export const ProductList: React.FC<ProductListProps> = ({ products, isLoading, onDelete }) => {
  if (isLoading) {
    return <div className="loading-state">Cargando datos desde la API...</div>;
  }

  if (products.length === 0) {
    return (
      <div className="empty-state">
        <p>No hay productos registrados en la base de datos.</p>
        <span>Utiliza el formulario contiguo para registrar el primero.</span>
      </div>
    );
  }

  return (
    <div className="product-grid">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} onDelete={onDelete} />
      ))}
    </div>
  );
};

import React from 'react';
import type { Product } from '../types/product';

interface ProductCardProps {
  product: Product;
  onDelete?: (id: number) => void;
  onBuy?: (product: Product) => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product, onDelete, onBuy }) => {
  return (
    <div className="product-card">
      <div className="card-header">
        <span className="category-tag">{product.category || 'General'}</span>
        <span className="product-price">${product.price?.toFixed(2)}</span>
      </div>
      <h3 className="product-title">{product.name}</h3>
      <p className="product-desc">{product.description}</p>
      <div className="card-footer">
        <span className="product-id">ID: #{product.id}</span>
        <div className="card-actions">
          {onBuy && (
            <button
              className="btn-buy"
              onClick={() => onBuy(product)}
              title="Comprar este producto"
            >
              Comprar
            </button>
          )}
          {onDelete && product.id && (
            <button
              className="btn-delete"
              onClick={() => onDelete(product.id!)}
              title="Eliminar producto"
            >
              Eliminar
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

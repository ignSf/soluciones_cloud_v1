import React from 'react';
import type { Product } from '../types/product';

interface ProductCardProps {
  product: Product;
  stock?: number;
  onDelete?: (id: number) => void;
  onBuy?: (product: Product) => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product, stock, onDelete, onBuy }) => {
  const isOutOfStock = stock !== undefined && stock <= 0;

  return (
    <div className="product-card">
      <div className="card-header">
        <span className="category-tag">{product.category || 'General'}</span>
        <span className="product-price">${product.price?.toFixed(2)}</span>
      </div>
      
      <h3 className="product-title">{product.name}</h3>
      <p className="product-desc">{product.description}</p>

      {/* Indicador de Stock sincronizado con RabbitMQ */}
      {stock !== undefined && (
        <div className="product-stock-wrapper">
          {stock > 0 ? (
            <span className="stock-badge stock-in">
              <span className="stock-dot-in"></span> Stock: {stock} u.
              <span className="stock-mq-pill" title="Gestionado por inventory-service vía RabbitMQ">RabbitMQ</span>
            </span>
          ) : (
            <span className="stock-badge stock-empty">
              <span className="stock-dot-out"></span> Agotado
            </span>
          )}
        </div>
      )}

      <div className="card-footer">
        <span className="product-id">ID: #{product.id}</span>
        <div className="card-actions">
          {onBuy && (
            <button
              className="btn-buy"
              onClick={() => onBuy(product)}
              disabled={isOutOfStock}
              title={isOutOfStock ? 'Producto sin stock disponible' : 'Comprar este producto'}
            >
              {isOutOfStock ? 'Agotado' : 'Comprar'}
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

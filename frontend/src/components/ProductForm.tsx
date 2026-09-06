import React, { useState } from 'react';
import type { Product } from '../types/product';

interface ProductFormProps {
  onProductCreated: (product: Product) => Promise<void>;
}

export const ProductForm: React.FC<ProductFormProps> = ({ onProductCreated }) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [category, setCategory] = useState('Hardware');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !price) return;

    try {
      setIsSubmitting(true);
      await onProductCreated({
        name,
        description,
        price: parseFloat(price),
        category,
      });
      // Limpiar formulario
      setName('');
      setDescription('');
      setPrice('');
      setCategory('Hardware');
    } catch (err) {
      alert('Error al guardar el producto');
      console.error(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="form-card">
      <h3 className="form-title">Registrar Nuevo Producto</h3>
      <p className="form-subtitle">Los datos se persistirán en la base de datos H2.</p>
      
      <form onSubmit={handleSubmit} className="product-form">
        <div className="form-group">
          <label htmlFor="name">Nombre del Producto</label>
          <input
            id="name"
            type="text"
            required
            placeholder="Ej. Teclado Mecánico RGB"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="category">Categoría</label>
          <select
            id="category"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            <option value="Hardware">Hardware</option>
            <option value="Periféricos">Periféricos</option>
            <option value="Servicios Cloud">Servicios Cloud</option>
            <option value="Software">Software</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="price">Precio (USD)</label>
          <input
            id="price"
            type="number"
            step="0.01"
            min="0"
            required
            placeholder="99.99"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Descripción</label>
          <textarea
            id="description"
            rows={3}
            placeholder="Detalles y especificaciones técnicas..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <button type="submit" className="btn-submit" disabled={isSubmitting}>
          {isSubmitting ? 'Guardando en BD...' : 'Guardar Producto'}
        </button>
      </form>
    </div>
  );
};

import { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import type { Product } from './types/product';
import { productService } from './services/productService';
import { Navbar } from './components/Navbar';
import { ProductList } from './components/ProductList';
import { ProductForm } from './components/ProductForm';
import './index.css';

export function App() {
  const auth = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Obtener estrictamente el access_token para autorizar llamadas a la API (NUNCA el id_token)
  const userToken = auth.user?.access_token;

  const fetchProducts = async () => {
    try {
      setIsLoading(true);
      setErrorMessage(null);
      const data = await productService.getAll();
      setProducts(data);
    } catch (err: any) {
      console.error(err);
      setErrorMessage('No se pudo conectar con el servicio de productos. Asegúrese de que el backend esté en ejecución.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleCreateProduct = async (newProduct: Product) => {
    if (!auth.isAuthenticated) {
      alert('⚠️ Acción no autorizada: Debes iniciar sesión con AWS Cognito en el botón superior para crear productos.');
      return;
    }

    try {
      const created = await productService.create(newProduct, userToken);
      setProducts((prev) => [...prev, created]);
    } catch (err: any) {
      alert(err.message || 'Error al guardar el producto');
      console.error(err);
    }
  };

  const handleDeleteProduct = async (id: number) => {
    if (!auth.isAuthenticated) {
      alert('⚠️ Acción no autorizada: Debes iniciar sesión con AWS Cognito para eliminar productos.');
      return;
    }

    if (!window.confirm('¿Seguro que deseas eliminar este producto de la base de datos?')) {
      return;
    }

    try {
      await productService.delete(id, userToken);
      setProducts((prev) => prev.filter((p) => p.id !== id));
    } catch (err: any) {
      alert(err.message || 'Error al eliminar el producto');
      console.error(err);
    }
  };

  return (
    <div className="app-layout">
      <Navbar itemCount={products.length} />

      <main className="main-content">
        {errorMessage && (
          <div className="error-banner">
            <p><strong>Aviso de Conexión:</strong> {errorMessage}</p>
            <button onClick={fetchProducts} className="btn-retry">Reintentar Conexión</button>
          </div>
        )}

        <div className="content-grid">
          <section className="column-form">
            <ProductForm onProductCreated={handleCreateProduct} />
          </section>

          <section className="column-list">
            <div className="list-header">
              <h3>Catálogo de Productos Persistidos</h3>
              <button onClick={fetchProducts} className="btn-refresh" title="Recargar">
                ↻ Refrescar
              </button>
            </div>
            <ProductList
              products={products}
              isLoading={isLoading}
              onDelete={handleDeleteProduct}
            />
          </section>
        </div>
      </main>
    </div>
  );
}

export default App;

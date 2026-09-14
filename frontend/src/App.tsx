import { useState, useEffect } from 'react';
import { useAuth } from './auth/useAuth';
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

  // Control de pantalla: 'inicio' | 'tienda' | 'inventario'
  const [pagina, setPagina] = useState<'inicio' | 'tienda' | 'inventario'>('inicio');

  // El access_token es el que autoriza contra la API; el id_token queda en el front
  const userToken = auth.user?.access_token;

  const fetchProducts = async () => {
    try {
      setIsLoading(true);
      setErrorMessage(null);
      const data = await productService.getAll();
      setProducts(data);
    } catch (err: any) {
      setErrorMessage('No se pudo conectar con el Backend en Render (https://soluciones-cloud-v1.onrender.com). Asegúrate de que el servicio haya terminado de desplegar.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleCreateProduct = async (newProduct: Product) => {
    if (!auth.isAuthenticated) {
      alert('Acción no autorizada: Debes iniciar sesión con Microsoft Entra ID en el botón superior para crear productos.');
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
      alert('Acción no autorizada: Debes iniciar sesión con Microsoft Entra ID para eliminar productos.');
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
      <Navbar 
        itemCount={products.length} 
        paginaActual={pagina}
        onCambiarPagina={setPagina}
      />

      <main className="main-content">
        {errorMessage && (
          <div className="error-banner">
            <p><strong>Aviso de Conexión:</strong> {errorMessage}</p>
            <button onClick={fetchProducts} className="btn-retry">Reintentar Conexión</button>
          </div>
        )}

        {/* 1. Vista de Bienvenida (Inicio / Home) */}
        {pagina === 'inicio' && (
          <section className="home-welcome">
            <span className="home-badge">CloudStore — Microsoft Entra ID</span>
            <h1 className="home-title">Bienvenido a Nuestra Tienda</h1>
            <p className="home-subtitle">
              Plataforma conectada a backend en la nube con autenticación segura.
              Explora nuestros productos o ingresa a gestionar el inventario.
            </p>

            <div className="home-actions">
              <button 
                className="btn-enter-store" 
                onClick={() => setPagina('tienda')}
              >
                Explorar Tienda ({products.length} productos)
              </button>

              {!auth.isAuthenticated ? (
                <button 
                  className="btn-cognito-home" 
                  onClick={() => auth.signinRedirect()}
                >
                  Iniciar Sesión con Microsoft
                </button>
              ) : (
                <div className="home-user-badge">
                  <span>Conectado: <strong>{auth.user?.profile.email || auth.user?.profile.sub}</strong></span>
                  <button 
                    className="btn-primary-small"
                    onClick={() => setPagina('inventario')}
                  >
                    Ir al Inventario
                  </button>
                  <button 
                    className="btn-logout" 
                    onClick={() => auth.removeUser()}
                  >
                    Cerrar sesión
                  </button>
                </div>
              )}
            </div>
          </section>
        )}

        {/* 2. Vista Pública de Tienda (Catálogo de Productos) */}
        {pagina === 'tienda' && (
          <section className="store-page">
            <div className="store-header">
              <div>
                <h1 className="store-title">Catálogo de Productos</h1>
                <p className="store-subtitle">
                  Explora todos los artículos disponibles en nuestra tienda en la nube
                </p>
              </div>
              <button onClick={fetchProducts} className="btn-refresh" title="Recargar catálogo">
                ↻ Refrescar
              </button>
            </div>

            <ProductList
              products={products}
              isLoading={isLoading}
            />
          </section>
        )}

        {/* 3. Vista de Administración de Inventario (Solo con cuenta logueada) */}
        {pagina === 'inventario' && (
          !auth.isAuthenticated ? (
            <section className="admin-lock-card">
              <h2 className="lock-title">Acceso Restringido a Administradores</h2>
              <p className="lock-description">
                La sección de <strong>Administrar Inventario</strong> permite dar de alta y eliminar artículos. 
                Para acceder a estas herramientas debes iniciar sesión previamente con tu cuenta de <strong>Microsoft Entra ID</strong>.
              </p>
              <button 
                className="btn-cognito-home" 
                onClick={() => auth.signinRedirect()}
              >
                Iniciar Sesión con Microsoft
              </button>
            </section>
          ) : (
            <div className="content-grid">
              <section className="column-form">
                <ProductForm onProductCreated={handleCreateProduct} />
              </section>

              <section className="column-list">
                <div className="list-header">
                  <h3>Inventario de Productos Persistidos</h3>
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
          )
        )}
      </main>
    </div>
  );
}

export default App;

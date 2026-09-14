import { useState, useEffect } from 'react';
import { useAuth } from './auth/useAuth';
import type { Product } from './types/product';
import type { Order } from './types/order';
import { productService } from './services/productService';
import { orderService } from './services/orderService';
import { Navbar } from './components/Navbar';
import { ProductList } from './components/ProductList';
import { ProductForm } from './components/ProductForm';
import narutoImage from './assets/homeimages/naruto.png';
import cloudMain from './assets/homeimages/cloud_6.png';
import cloudLeft from './assets/homeimages/cloud_4.png';
import cloudRight from './assets/homeimages/cloud_12.png';
import cloudPass1 from './assets/homeimages/cloud_2.png';
import cloudPass2 from './assets/homeimages/cloud_10.png';
import './index.css';

export function App() {
  const auth = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isLoadingOrders, setIsLoadingOrders] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [orderSuccessMessage, setOrderSuccessMessage] = useState<string | null>(null);

  // Control de pantalla: 'inicio' | 'tienda' | 'inventario' | 'pedidos'
  const [pagina, setPagina] = useState<'inicio' | 'tienda' | 'inventario' | 'pedidos'>('inicio');

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

  const fetchOrders = async () => {
    if (!auth.isAuthenticated || !userToken) return;
    try {
      setIsLoadingOrders(true);
      const data = await orderService.getMyOrders(userToken);
      setOrders(data);
    } catch (err: any) {
      console.warn('Aviso al consultar órdenes:', err);
    } finally {
      setIsLoadingOrders(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  useEffect(() => {
    if (pagina === 'pedidos' && auth.isAuthenticated) {
      fetchOrders();
    }
  }, [pagina, auth.isAuthenticated, userToken]);

  const handleCreateProduct = async (newProduct: Product) => {
    if (!auth.isAuthenticated) {
      alert('Acción no autorizada: Debes iniciar sesión con Microsoft Entra ID o AWS Cognito para crear productos.');
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
      alert('Acción no autorizada: Debes iniciar sesión con Microsoft Entra ID o AWS Cognito para eliminar productos.');
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

  const handleBuyProduct = async (product: Product) => {
    if (!auth.isAuthenticated) {
      alert('Para realizar un pedido en la tienda, primero debes iniciar sesión con Microsoft Entra ID o AWS Cognito.');
      return;
    }

    if (!window.confirm(`¿Deseas confirmar la compra de "${product.name}" por $${product.price?.toFixed(2)}?`)) {
      return;
    }

    try {
      const newOrder: Order = {
        productId: product.id!,
        productName: product.name,
        quantity: 1,
        unitPrice: product.price,
        totalAmount: product.price,
        status: 'CONFIRMED',
      };
      const created = await orderService.create(newOrder, userToken);
      setOrders((prev) => [created, ...prev]);
      setOrderSuccessMessage(`¡Pedido #${created.id ?? ''} generado con éxito para ${product.name}! Puedes revisarlo en la pestaña "Mis Pedidos".`);
      setTimeout(() => setOrderSuccessMessage(null), 6000);
    } catch (err: any) {
      alert(err.message || 'Error al procesar la compra en el microservicio de órdenes');
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

        {orderSuccessMessage && (
          <div className="success-banner">
            <p><strong>Pedido Confirmado:</strong> {orderSuccessMessage}</p>
            <button onClick={() => setPagina('pedidos')} className="btn-success-action">Ver Mis Pedidos</button>
          </div>
        )}

        {/* 1. Vista de Bienvenida (Inicio / Home) */}
        {pagina === 'inicio' && (
          <div className="home-hero-container">
            <section className="home-welcome">
              <span className="home-badge">CloudStore — Microservicios en la Nube</span>
              <h1 className="home-title">Bienvenido a Nuestra Tienda</h1>
              <p className="home-subtitle">
                Plataforma desacoplada con microservicio de catálogo, microservicio de órdenes y autenticación multi-cloud con AWS Cognito y Microsoft Entra ID.
              </p>

              <div className="home-actions">
                <button 
                  className="btn-enter-store" 
                  onClick={() => setPagina('tienda')}
                >
                  Explorar Tienda ({products.length} productos)
                </button>

                {!auth.isAuthenticated ? (
                  <div className="home-auth-buttons">
                    <button 
                      className="btn-login-ms-home" 
                      onClick={() => auth.signinMicrosoft()}
                      title="Iniciar sesión mediante Microsoft Entra ID"
                    >
                      Iniciar Sesión con Microsoft
                    </button>
                    <button 
                      className="btn-cognito-home" 
                      onClick={() => auth.signinCognito()}
                      title="Iniciar sesión mediante AWS Cognito"
                    >
                      Iniciar Sesión con Cognito
                    </button>
                  </div>
                ) : (
                  <div className="home-user-badge">
                    <span>
                      Conectado ({auth.providerName}): <strong>{auth.user?.profile.email || auth.user?.profile.sub}</strong>
                    </span>
                    <button 
                      className="btn-primary-small"
                      onClick={() => setPagina('pedidos')}
                    >
                      Mis Pedidos
                    </button>
                    {auth.isAdmin && (
                      <button 
                        className="btn-primary-small"
                        onClick={() => setPagina('inventario')}
                      >
                        Ir al Inventario
                      </button>
                    )}
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

            <div className="home-character-wrapper">
              <img src={narutoImage} alt="Naruto" className="home-character-img" />
              <div className="home-clouds-cluster" aria-hidden="true">
                <img src={cloudLeft} alt="" className="home-cloud cloud-accent-left" />
                <img src={cloudMain} alt="" className="home-cloud cloud-main-bottom" />
                <img src={cloudRight} alt="" className="home-cloud cloud-accent-right" />
                <img src={cloudPass1} alt="" className="home-cloud cloud-dynamic-pass-1" />
                <img src={cloudPass2} alt="" className="home-cloud cloud-dynamic-pass-2" />
              </div>
            </div>
          </div>
        )}

        {/* 2. Vista Pública de Tienda (Catálogo de Productos) */}
        {pagina === 'tienda' && (
          <section className="store-page">
            <div className="store-header">
              <div>
                <h1 className="store-title">Catálogo de Productos</h1>
                <p className="store-subtitle">
                  Explora nuestros artículos y haz clic en "Comprar" para generar un pedido con tu cuenta
                </p>
              </div>
              <button onClick={fetchProducts} className="btn-refresh" title="Recargar catálogo">
                ↻ Refrescar
              </button>
            </div>

            <ProductList
              products={products}
              isLoading={isLoading}
              onBuy={handleBuyProduct}
            />
          </section>
        )}

        {/* 3. Vista de Mis Pedidos (Historial del Usuario Autenticado) */}
        {pagina === 'pedidos' && (
          !auth.isAuthenticated ? (
            <section className="admin-lock-card">
              <h2 className="lock-title">Historial de Pedidos Protegido</h2>
              <p className="lock-description">
                Para consultar tus compras o realizar pedidos en la tienda, debes iniciar sesión previamente con tu cuenta de <strong>Microsoft Entra ID</strong> o <strong>AWS Cognito</strong>.
              </p>
              <div className="lock-auth-buttons">
                <button 
                  className="btn-login-ms-home" 
                  onClick={() => auth.signinMicrosoft()}
                >
                  Iniciar Sesión con Microsoft
                </button>
                <button 
                  className="btn-cognito-home" 
                  onClick={() => auth.signinCognito()}
                >
                  Iniciar Sesión con Cognito
                </button>
              </div>
            </section>
          ) : (
            <section className="orders-page">
              <div className="orders-header">
                <div>
                  <h1 className="orders-title">Mis Pedidos</h1>
                  <p className="orders-subtitle">
                    Compras registradas en el microservicio de órdenes para: <strong>{auth.user?.profile.email || auth.user?.profile.sub}</strong>
                  </p>
                </div>
                <div className="orders-header-actions">
                  <button onClick={() => setPagina('tienda')} className="btn-enter-store-small">
                    + Comprar Más Artículos
                  </button>
                  <button onClick={fetchOrders} className="btn-refresh" title="Recargar pedidos">
                    ↻ Refrescar
                  </button>
                </div>
              </div>

              {isLoadingOrders ? (
                <div className="loading-state">Cargando pedidos desde el microservicio...</div>
              ) : orders.length === 0 ? (
                <div className="empty-state">
                  <p>Aún no has registrado pedidos con esta cuenta.</p>
                  <span>Visita la Tienda y haz clic en "Comprar" en cualquiera de los productos disponibles.</span>
                  <button onClick={() => setPagina('tienda')} className="btn-primary-small" style={{ marginTop: '1rem' }}>
                    Ir a la Tienda
                  </button>
                </div>
              ) : (
                <div className="orders-grid">
                  {orders.map((order) => (
                    <div key={order.id} className="order-card">
                      <div className="order-card-header">
                        <span className="order-id">Orden #{order.id}</span>
                        <span className="order-status-badge">
                          {order.status || 'CONFIRMADO'}
                        </span>
                      </div>
                      <h3 className="order-product-title">{order.productName || `Producto #${order.productId}`}</h3>
                      <div className="order-meta-info">
                        <p>Cantidad: <strong>{order.quantity || 1}</strong></p>
                        <p>Precio Unitario: <strong>${order.unitPrice?.toFixed(2)}</strong></p>
                        <p className="order-total-price">Total: <strong>${order.totalAmount?.toFixed(2)}</strong></p>
                      </div>
                      <div className="order-footer">
                        <span className="order-date">
                          {order.createdAt ? new Date(order.createdAt).toLocaleString('es-CL') : 'Recién creada'}
                        </span>
                        <span className="order-provider-tag">{auth.providerName}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          )
        )}

        {/* 4. Vista de Administración de Inventario (Solo con cuenta logueada y rol Admin) */}
        {pagina === 'inventario' && (
          !auth.isAuthenticated ? (
            <section className="admin-lock-card">
              <h2 className="lock-title">Acceso Restringido a Administradores</h2>
              <p className="lock-description">
                La sección de <strong>Administrar Inventario</strong> permite dar de alta y eliminar artículos. 
                Para acceder a estas herramientas debes iniciar sesión previamente con tu cuenta de <strong>Microsoft Entra ID</strong> o <strong>AWS Cognito</strong>.
              </p>
              <div className="lock-auth-buttons">
                <button 
                  className="btn-login-ms-home" 
                  onClick={() => auth.signinMicrosoft()}
                >
                  Iniciar Sesión con Microsoft
                </button>
                <button 
                  className="btn-cognito-home" 
                  onClick={() => auth.signinCognito()}
                >
                  Iniciar Sesión con Cognito
                </button>
              </div>
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

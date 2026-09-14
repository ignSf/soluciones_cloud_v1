import React from 'react';
import { useAuth } from '../auth/useAuth';

interface NavbarProps {
  itemCount: number;
  paginaActual: 'inicio' | 'tienda' | 'inventario';
  onCambiarPagina: (pagina: 'inicio' | 'tienda' | 'inventario') => void;
}

export const Navbar: React.FC<NavbarProps> = ({ itemCount, paginaActual, onCambiarPagina }) => {
  const auth = useAuth();

  const handleSignOut = () => {
    // Cierra la sesión local en el cliente
    auth.removeUser();
  };

  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand" onClick={() => onCambiarPagina('inicio')} style={{ cursor: 'pointer' }}>
          <span className="brand-badge">CloudStore</span>
          <h2>Tienda Online</h2>
        </div>

        <nav className="navbar-nav">
          <button 
            className={`nav-btn ${paginaActual === 'inicio' ? 'active' : ''}`}
            onClick={() => onCambiarPagina('inicio')}
          >
            Inicio
          </button>
          <button 
            className={`nav-btn ${paginaActual === 'tienda' ? 'active' : ''}`}
            onClick={() => onCambiarPagina('tienda')}
          >
            Tienda
          </button>
          {auth.isAdmin && (
            <button 
              className={`nav-btn ${paginaActual === 'inventario' ? 'active' : ''}`}
              onClick={() => onCambiarPagina('inventario')}
              title="Gestionar catálogo de productos"
            >
              Administrar Inventario
            </button>
          )}
        </nav>

        <div className="navbar-actions">
          <div className="navbar-status">
            <span className="status-dot"></span>
            <span className="status-text">{itemCount} productos</span>
          </div>

          {auth.isLoading ? (
            <span className="auth-loading">Verificando sesión...</span>
          ) : auth.isAuthenticated ? (
            <div className="user-profile">
              <span className="user-badge" title={`Usuario autenticado por ${auth.providerName}`}>
                {auth.user?.profile.email || auth.user?.profile.sub || 'Usuario'}
                <small style={{ display: 'block', fontSize: '0.75rem', opacity: 0.8 }}>({auth.providerName})</small>
              </span>
              <button onClick={handleSignOut} className="btn-logout" title="Cerrar sesión">
                Cerrar sesión
              </button>
            </div>
          ) : (
            <div className="navbar-auth-buttons">
              <button
                onClick={() => auth.signinMicrosoft()}
                className="btn-login btn-login-ms"
                title="Iniciar sesión mediante Microsoft Entra ID"
              >
                Iniciar sesión con Microsoft
              </button>
              <button
                onClick={() => auth.signinCognito()}
                className="btn-login btn-login-cognito"
                title="Iniciar sesión mediante AWS Cognito"
              >
                Iniciar sesión con Cognito
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;

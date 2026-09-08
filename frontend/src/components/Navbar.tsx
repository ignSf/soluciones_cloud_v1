import React from 'react';
import { useAuth } from 'react-oidc-context';

interface NavbarProps {
  itemCount: number;
}

export const Navbar: React.FC<NavbarProps> = ({ itemCount }) => {
  const auth = useAuth();

  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <span className="brand-badge">Cloud Demo</span>
          <h2>Panel de Gestión de Productos</h2>
        </div>

        <div className="navbar-actions">
          <div className="navbar-status">
            <span className="status-dot"></span>
            <span className="status-text">{itemCount} registros</span>
          </div>

          <div className="auth-section">
            {auth.isLoading ? (
              <span className="auth-loading">Verificando sesión...</span>
            ) : auth.isAuthenticated ? (
              <div className="user-profile">
                <span className="user-email" title={auth.user?.profile?.email || 'Usuario autenticado'}>
                  👤 {auth.user?.profile?.email || auth.user?.profile?.preferred_username || 'Usuario'}
                </span>
                <button
                  onClick={() => auth.signoutRedirect()}
                  className="btn-auth btn-logout"
                >
                  Cerrar Sesión
                </button>
              </div>
            ) : (
              <button
                onClick={() => auth.signinRedirect()}
                className="btn-auth btn-login"
              >
                🔐 Iniciar Sesión con Cognito
              </button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

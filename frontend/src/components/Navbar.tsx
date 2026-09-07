import React from 'react';
import { useAuth } from 'react-oidc-context';

interface NavbarProps {
  itemCount: number;
}

export const Navbar: React.FC<NavbarProps> = ({ itemCount }) => {
  const auth = useAuth();

  const handleSignOut = () => {
    // Cierra la sesión local en el cliente
    auth.removeUser();
  };

  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <span className="brand-badge">AWS Cognito IDaaS</span>
          <h2>Panel de Gestión de Productos</h2>
        </div>

        <div className="navbar-actions">
          <div className="navbar-status">
            <span className="status-dot"></span>
            <span className="status-text">{itemCount} productos</span>
          </div>

          {auth.isLoading ? (
            <span className="auth-loading">Verificando sesión...</span>
          ) : auth.isAuthenticated ? (
            <div className="user-profile">
              <span className="user-badge" title="Usuario autenticado por AWS Cognito">
                👤 {auth.user?.profile.email || auth.user?.profile.sub || 'Usuario'}
              </span>
              <button onClick={handleSignOut} className="btn-logout" title="Cerrar sesión">
                Cerrar sesión
              </button>
            </div>
          ) : (
            <button
              onClick={() => auth.signinRedirect()}
              className="btn-login"
              title="Iniciar sesión mediante AWS Cognito"
            >
              Iniciar sesión con Cognito
            </button>
          )}
        </div>
      </div>
    </header>
  );
};

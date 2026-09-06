import React from 'react';

interface NavbarProps {
  itemCount: number;
}

export const Navbar: React.FC<NavbarProps> = ({ itemCount }) => {
  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <span className="brand-badge">Cloud Demo</span>
          <h2>Panel de Gestión de Productos</h2>
        </div>
        <div className="navbar-status">
          <span className="status-dot"></span>
          <span className="status-text">Backend Conectado ({itemCount} registros)</span>
        </div>
      </div>
    </header>
  );
};

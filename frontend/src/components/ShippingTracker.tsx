import React, { useState, useEffect } from 'react';
import type { Shipment } from '../types/shipping';
import { shippingService } from '../services/shippingService';

interface ShippingTrackerProps {
  initialTrackingNumber?: string;
  onGoToStore?: () => void;
}

export const ShippingTracker: React.FC<ShippingTrackerProps> = ({
  initialTrackingNumber,
  onGoToStore,
}) => {
  const [trackingInput, setTrackingInput] = useState<string>(initialTrackingNumber || '');
  const [currentShipment, setCurrentShipment] = useState<Shipment | null>(null);
  const [recentShipments, setRecentShipments] = useState<Shipment[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isUpdating, setIsUpdating] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successNotice, setSuccessNotice] = useState<string | null>(null);

  // Cargar envíos recientes
  const loadRecentShipments = async () => {
    try {
      const all = await shippingService.getAll();
      setRecentShipments(all);
    } catch (e) {
      console.warn('Error al cargar envíos recientes:', e);
    }
  };

  useEffect(() => {
    loadRecentShipments();
  }, []);

  // Si nos pasan un tracking inicial, buscarlo automáticamente
  useEffect(() => {
    if (initialTrackingNumber) {
      setTrackingInput(initialTrackingNumber);
      handleSearch(initialTrackingNumber);
    }
  }, [initialTrackingNumber]);

  const handleSearch = async (codeToSearch?: string) => {
    const code = (codeToSearch || trackingInput).trim();
    if (!code) {
      setErrorMessage('Por favor ingresa un código de seguimiento válido (ej. TRK-CL-XXXXXXXX).');
      return;
    }

    setIsLoading(true);
    setErrorMessage(null);
    setSuccessNotice(null);

    try {
      const result = await shippingService.getByTracking(code);
      if (!result) {
        setErrorMessage(`No se encontró ningún despacho asociado al código ${code}.`);
        setCurrentShipment(null);
      } else {
        setCurrentShipment(result);
      }
    } catch (err: any) {
      setErrorMessage('Error al consultar el microservicio de envíos.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStatusChange = async (newStatus: 'PREPARING' | 'IN_TRANSIT' | 'DELIVERED') => {
    if (!currentShipment) return;
    setIsUpdating(true);
    try {
      const updated = await shippingService.updateStatus(currentShipment.id, newStatus);
      setCurrentShipment(updated);
      setSuccessNotice(`Estado logístico actualizado a: ${newStatus}`);
      // Actualizar también en la lista de recientes
      setRecentShipments((prev) =>
        prev.map((s) => (s.id === updated.id ? updated : s))
      );
      setTimeout(() => setSuccessNotice(null), 4000);
    } catch (err) {
      alert('No se pudo actualizar el estado.');
    } finally {
      setIsUpdating(false);
    }
  };

  // Determinar paso actual del stepper (1 a 4)
  const getStepProgress = (status?: string): number => {
    switch (status) {
      case 'PREPARING':
        return 2;
      case 'IN_TRANSIT':
        return 3;
      case 'DELIVERED':
        return 4;
      default:
        return 2;
    }
  };

  const stepLevel = getStepProgress(currentShipment?.status);

  return (
    <section className="tracker-page">
      {/* Encabezado */}
      <div className="tracker-header">
        <div>
          <div className="tracker-badge-cluster">
            <span className="badge-broker-lavin">LavinMQ AMQP 0-9-1</span>
            <span className="badge-service-port">shipping-service (:8083)</span>
          </div>
          <h1 className="tracker-title">Rastreo de Envíos y Despacho</h1>
          <p className="tracker-subtitle">
            Seguimiento logístico desacoplado en tiempo real. Cada guía es emitida automáticamente por el broker <strong>LavinMQ</strong> al confirmarse una orden en el backend.
          </p>
        </div>
      </div>

      {/* Barra de búsqueda interactiva */}
      <div className="tracker-search-card">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch();
          }}
          className="tracker-search-form"
        >
          <div className="tracker-search-input-wrap">
            <svg className="search-svg-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              type="text"
              className="tracker-search-input"
              placeholder="Ingresa tu número de guía (ej. TRK-CL-48192031)..."
              value={trackingInput}
              onChange={(e) => setTrackingInput(e.target.value)}
            />
          </div>
          <button type="submit" className="btn-tracker-search" disabled={isLoading}>
            {isLoading ? 'Rastreando...' : 'Buscar Envío'}
          </button>
        </form>

        {/* Chips de acceso rápido */}
        {recentShipments.length > 0 && (
          <div className="tracker-quick-chips">
            <span className="chips-label">Guías recientes en el broker:</span>
            <div className="chips-list">
              {recentShipments.slice(0, 5).map((s) => (
                <button
                  key={s.id}
                  className={`chip-btn ${currentShipment?.trackingNumber === s.trackingNumber ? 'chip-active' : ''}`}
                  onClick={() => {
                    setTrackingInput(s.trackingNumber);
                    handleSearch(s.trackingNumber);
                  }}
                >
                  <span className="chip-code">{s.trackingNumber}</span>
                  <span className={`chip-status chip-${s.status.toLowerCase()}`}>
                    {s.status}
                  </span>
                </button>
              ))}
            </div>
          </div>
        )}
      </div>

      {errorMessage && (
        <div className="tracker-error-banner">
          {errorMessage}
        </div>
      )}

      {successNotice && (
        <div className="success-banner">
          <span>{successNotice}</span>
        </div>
      )}

      {/* Detalle del Despacho y Stepper Visual */}
      {currentShipment ? (
        <div className="shipment-detail-card">
          <div className="shipment-card-top">
            <div>
              <span className="shipment-order-tag">Orden Asociada: #{currentShipment.orderId}</span>
              <h2 className="shipment-tracking-code">{currentShipment.trackingNumber}</h2>
            </div>
            <div className="shipment-status-pill-wrap">
              <span className={`shipment-status-pill status-${currentShipment.status.toLowerCase()}`}>
                {currentShipment.status === 'PREPARING' && 'En Preparación'}
                {currentShipment.status === 'IN_TRANSIT' && 'En Tránsito'}
                {currentShipment.status === 'DELIVERED' && 'Entregado'}
              </span>
            </div>
          </div>

          {/* Stepper Gráfico con Números Profesionales */}
          <div className="tracking-stepper">
            <div className="stepper-progress-track">
              <div
                className="stepper-progress-fill"
                style={{ width: `${((stepLevel - 1) / 3) * 100}%` }}
              ></div>
            </div>

            <div className="stepper-steps">
              {/* Paso 1 */}
              <div className="stepper-step completed">
                <div className="step-circle">1</div>
                <div className="step-label">Orden Creada</div>
                <div className="step-sub">Backend demo (:8080)</div>
              </div>

              {/* Paso 2 */}
              <div className={`stepper-step ${stepLevel >= 2 ? 'active-step' : ''}`}>
                <div className="step-circle">2</div>
                <div className="step-label">Bodega Central</div>
                <div className="step-sub">LavinMQ (PREPARING)</div>
              </div>

              {/* Paso 3 */}
              <div className={`stepper-step ${stepLevel >= 3 ? 'active-step' : ''}`}>
                <div className="step-circle">3</div>
                <div className="step-label">En Tránsito</div>
                <div className="step-sub">Transportista en ruta</div>
              </div>

              {/* Paso 4 */}
              <div className={`stepper-step ${stepLevel >= 4 ? 'active-step' : ''}`}>
                <div className="step-circle">4</div>
                <div className="step-label">Entregado</div>
                <div className="step-sub">Recepción confirmada</div>
              </div>
            </div>
          </div>

          {/* Cuadrícula de Información Logística */}
          <div className="shipment-info-grid">
            <div className="info-box">
              <span className="info-label">Destinatario</span>
              <span className="info-value">{currentShipment.userEmail || 'Cliente Registrado'}</span>
            </div>
            <div className="info-box">
              <span className="info-label">Transportista</span>
              <span className="info-value">{currentShipment.carrier || 'Starken Cloud Express'}</span>
            </div>
            <div className="info-box">
              <span className="info-label">Destino</span>
              <span className="info-value">{currentShipment.destinationAddress || 'Santiago, Chile'}</span>
            </div>
            <div className="info-box">
              <span className="info-label">Fecha de Despacho</span>
              <span className="info-value">
                {currentShipment.createdAt
                  ? new Date(currentShipment.createdAt).toLocaleString('es-CL')
                  : 'Reciente'}
              </span>
            </div>
          </div>

          {/* Panel Interactivo de Demostración */}
          <div className="demo-simulator-card">
            <div className="demo-simulator-header">
              <span className="simulator-title">Simulador Logístico (Control de Estados)</span>
              <span className="simulator-subtitle">
                Permite cambiar el estado del envío para probar en vivo la respuesta del microservicio <code>shipping-service</code>.
              </span>
            </div>
            <div className="simulator-actions">
              <button
                className={`btn-sim ${currentShipment.status === 'PREPARING' ? 'btn-sim-active' : ''}`}
                onClick={() => handleStatusChange('PREPARING')}
                disabled={isUpdating}
              >
                Preparación
              </button>
              <button
                className={`btn-sim ${currentShipment.status === 'IN_TRANSIT' ? 'btn-sim-active' : ''}`}
                onClick={() => handleStatusChange('IN_TRANSIT')}
                disabled={isUpdating}
              >
                En Tránsito
              </button>
              <button
                className={`btn-sim ${currentShipment.status === 'DELIVERED' ? 'btn-sim-active' : ''}`}
                onClick={() => handleStatusChange('DELIVERED')}
                disabled={isUpdating}
              >
                Entregado
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="tracker-empty-card">
          <div className="empty-icon-wrap">
            <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="empty-svg-icon">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
              <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
              <line x1="12" y1="22.08" x2="12" y2="12"></line>
            </svg>
          </div>
          <h3>Ingresa un código para rastrear tu envío</h3>
          <p>
            Al realizar cualquier compra en la <strong>Tienda</strong>, el broker <strong>LavinMQ</strong> genera una guía de despacho única.
          </p>
          {onGoToStore && (
            <button onClick={onGoToStore} className="btn-primary-small" style={{ marginTop: '1rem' }}>
              Ir a la Tienda a Realizar una Compra
            </button>
          )}
        </div>
      )}
    </section>
  );
};

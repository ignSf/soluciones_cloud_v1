# Soluciones Cloud - Arquitectura de Microservicios Tri-Broker

Proyecto de arquitectura Cloud desacoplada compuesto por backend principal, microservicios orientados a eventos, frontend modular en React + Vite y triple bus de mensajería asíncrona.

## Estructura del Repositorio

- `frontend/`: Aplicación cliente en React con TypeScript, Vite y componentes modulares desacoplados (Puerto `:5173`).
- `demo/`: Backend principal REST (Spring Boot). Gestiona `/api/products` (catálogo) y `/api/orders` (pedidos). Publica eventos a **RabbitMQ**, **Amazon SQS** y **LavinMQ** (Puerto `:8080`).
- `inventory-service/`: Microservicio de inventario y stock (Spring Boot). Escucha eventos de **RabbitMQ** para descontar stock (Puerto `:8081`).
- `notificaciones-service/`: Microservicio de avisos y correos (Spring Boot). Escucha eventos de **Amazon SQS** mediante long-polling (Puerto `:8082`).
- `shipping-service/`: Microservicio de despacho y logística (Spring Boot). Escucha eventos de **LavinMQ** para generar guías de despacho y tracking (Puerto `:8083`).

---

## Arquitectura Orientada a Eventos (Tri-Broker)

Al crear un pedido (`POST /api/orders`), `demo` guarda la compra y emite el evento asíncrono a 3 destinos simultáneos:

```
                                  +--> [RabbitMQ :5672] -----> inventory-service (:8081) [Descuenta Stock]
                                  |
POST /api/orders ---> demo (:8080)+--> [Amazon SQS (Cloud)] -> notificaciones-service (:8082) [Envía Correo]
                                  |
                                  +--> [LavinMQ :5673] ------> shipping-service (:8083) [Genera Tracking]
```

---

## Ejecución con Docker Compose

```bash
docker compose up --build
```

### Tabla de Servicios y Puertos

| Servicio / Broker | URL / Puerto | Descripción |
|---|---|---|
| **Frontend** | `http://localhost:5173` | Tienda Online |
| **Backend `demo`** | `http://localhost:8080/api/products` | Catálogo y Órdenes |
| **`inventory-service`** | `http://localhost:8081/api/inventory` | Stock (RabbitMQ) |
| **`notificaciones-service`** | `http://localhost:8082/api/notificaciones/colas/estado` | Avisos (Amazon SQS) |
| **`shipping-service`** | `http://localhost:8083/api/shipping` | Envíos y Tracking (LavinMQ) |
| **Panel RabbitMQ** | `http://localhost:15672` (guest / guest) | Gestión de RabbitMQ |
| **Panel LavinMQ** | `http://localhost:1234` (guest / guest) | Gestión de LavinMQ |

---

## Microservicio de Envíos (`shipping-service`) - LavinMQ

El microservicio `shipping-service` opera con **LavinMQ** (AMQP 0-9-1 de alto rendimiento):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/shipping` | Lista todos los despachos registrados |
| GET | `/api/shipping/order/{orderId}` | Consulta el estado y tracking de un pedido |
| GET | `/api/shipping/track/{trackingNumber}` | Rastrea un envío por su código `TRK-CL-XXXX` |
| PUT | `/api/shipping/{id}/status` | Actualiza estado (`PREPARING`, `IN_TRANSIT`, `DELIVERED`) |

- **Exchange**: `shipping.exchange` (Direct)
- **Routing Key**: `shipping.created`
- **Cola**: `shipping.order-created.queue`
- **Dead Letter Queue (DLQ)**: `shipping.order-created.dlq` vía `shipping.dlx`

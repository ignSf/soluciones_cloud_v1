# Soluciones Cloud - Fase 1: Base Funcional

Proyecto de demostración de arquitectura Cloud compuesto por un backend en Spring Boot y un frontend modular en React + Vite.

## Estructura del Repositorio

- `demo/`: Backend REST con Spring Boot, Spring Security (modo permisivo inicial) y base de datos relacional H2.
- `frontend/`: Aplicación cliente en React con TypeScript, Vite y componentes modulares desacoplados.
- `inventory-service/`: Microservicio de inventario (Spring Boot) que gestiona el stock de cada producto.

## Requisitos de Ejecución

### 1. Backend (Spring Boot)
- Java 21 o superior.
- Ejecutar:
  ```bash
  cd demo
  ./mvnw spring-boot:run
  ```
  API disponible en: `http://localhost:8080/api/products`  
  Consola H2 en: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)

### 2. Frontend (React + Vite)
- Node.js 18+ y npm.
- Ejecutar:
  ```bash
  cd frontend
  npm install
  npm run dev
  ```
  Interfaz disponible en: `http://localhost:5173`

### 3. Microservicio de inventario (Spring Boot)
- Java 21 o superior.
- Ejecutar:
  ```bash
  cd inventory-service
  ./mvnw spring-boot:run
  ```
  API disponible en: `http://localhost:8081/api/inventory`

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| GET | `/api/inventory` | Lista el inventario | Público |
| GET | `/api/inventory/{productId}` | Consulta el stock de un producto | Público |
| POST | `/api/inventory` | Crea el inventario de un producto | Rol `Admin` |
| PUT | `/api/inventory/{productId}` | Fija el stock a un valor exacto | Rol `Admin` |
| POST | `/api/inventory/{productId}/reserve` | Descuenta stock (409 si no alcanza) | Rol `Admin` |

- Usa la misma autenticación JWT que `demo` (Microsoft Entra ID o Cognito, rol `Admin`).
- Para probar con `curl` en local sin token: `INVENTORY_SECURITY_OPEN=true ./mvnw spring-boot:run`.
- Base de datos: H2 en memoria por defecto, o PostgreSQL (Supabase) con `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`. La tabla `inventory_items` está incluida en `supabase_schema.sql`.
- El campo `productId` corresponde al `id` de la tabla `products` de `demo`.

## Ejecución con Docker

Requisitos: Docker Desktop (o Docker Engine con el plugin Compose).

```bash
docker compose up --build
```

| Servicio | URL |
|---|---|
| Frontend | `http://localhost:5173` |
| Backend `demo` | `http://localhost:8080/api/products` |
| `inventory-service` | `http://localhost:8081/api/inventory` |
| Panel de RabbitMQ | `http://localhost:15672` (guest / guest) |

- Por defecto cada backend usa H2 en memoria. Para Supabase, copia `.env.example` a `.env` y completa las variables.
- Para probar el inventario sin token: `INVENTORY_SECURITY_OPEN=true docker compose up --build`.
- Detener todo: `docker compose down`.
- Las variables `VITE_*` del frontend se fijan al construir la imagen: si las cambias, vuelve a ejecutar con `--build`.

## Mensajería con RabbitMQ

Al crear un pedido, `demo` publica un evento y `inventory-service` descuenta el stock de forma asíncrona.

```
POST /api/orders -> demo guarda el pedido
                 -> publica OrderCreatedEvent en orders.exchange (routing key: orders.created)
                 -> cola inventory.order-created.queue
                 -> inventory-service (@RabbitListener) descuenta el stock
```

- Si el producto no existe o no hay stock suficiente, el mensaje se rechaza y va a la Dead Letter Queue `inventory.order-created.dlq`.
- Si RabbitMQ no está disponible al crear el pedido, `demo` guarda el pedido igual y registra el error en el log.
- Variables de conexión (ambos servicios): `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`. En Docker Compose el host es `rabbitmq`; en local, `localhost`.

### Cómo probarlo

1. `docker compose up --build`
2. Crea un pedido desde el frontend (con sesión iniciada) y consulta `http://localhost:8081/api/inventory`: el stock del producto comprado debe haber bajado.
3. Sin iniciar sesión: en el panel de RabbitMQ (`http://localhost:15672`) ve a **Exchanges > orders.exchange > Publish message**, usa la routing key `orders.created`, agrega la propiedad `content_type = application/json` y este payload:
   ```json
   {"orderId": 99, "userEmail": "prueba@cloudstore.local", "productId": 1, "quantity": 2, "totalAmount": 2999.98}
   ```
4. Para probar el rechazo, repite con `"quantity": 999` y revisa la cola `inventory.order-created.dlq` en la pestaña **Queues**.
5. Ver los logs: `docker compose logs -f inventory-service`.


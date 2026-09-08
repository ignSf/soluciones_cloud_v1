# Soluciones Cloud - Plataforma de Gestión de Productos Cloud

Arquitectura cloud-native compuesta por un backend REST en Spring Boot (OAuth2 Resource Server) y un frontend modular en React + TypeScript + Vite autenticado mediante AWS Cognito.

---

## 🏛️ Arquitectura del Sistema

```
[ Frontend (React + Vite) ]
        │  ▲
        │  │  1. Autenticación OIDC (Authorization Code Flow con PKCE)
        ▼  │
 [ AWS Cognito User Pool ]
        │
        │  2. Emite access_token JWT (RS256)
        ▼
[ Frontend (React) ] ── Bearer <access_token> ──► [ Backend (Spring Boot Resource Server) ]
                                                            │
                                                            ├── Valida JWT (iss, token_use=access, client_id, exp)
                                                            ├── Control de Acceso y CORS unificado
                                                            └── Base de Datos H2 (dev) / Relacional persistente
```

---

## 📦 Componentes y Tecnologías

### Backend (`demo/`)
* **Framework:** Spring Boot con Java 21 (validado con `maven-enforcer-plugin`).
* **Seguridad:** Spring Security OAuth2 Resource Server con integración de claves públicas JWKS de AWS Cognito.
* **Validación de Tokens:** `CognitoAccessTokenValidator` para verificar de forma obligatoria que `token_use == "access"` y el `client_id` autorizado, descartando automáticamente `id_token`.
* **Persistencia:** Spring Data JPA con H2 Database y validación declarativa Bean Validation (`@Valid`, `@NotBlank`, `@PositiveOrZero`).
* **Precisión Monetaria:** Precios representados en `BigDecimal` con precisión monetaria fija en BD (`numeric(12,2)`).
* **Manejo de Errores:** `@RestControllerAdvice` para capturar errores sin fugar información interna ni stack traces.

### Frontend (`frontend/`)
* **Framework:** React con TypeScript y Vite.
* **Autenticación OIDC:** `react-oidc-context` conectado al User Pool de AWS Cognito.
* **Seguridad de API:** Transmisión estricta del `access_token` en cabeceras `Authorization: Bearer <token>`.
* **Variables de Entorno:** Parametrización mediante `.env.development` y `.env.production` vía `import.meta.env.VITE_API_BASE_URL`.

---

## 🔐 Configuración de AWS Cognito

| Parámetro | Valor Configurado |
| :--- | :--- |
| **Región** | `us-east-1` |
| **User Pool ID** | `us-east-1_OL9DjB9XL` |
| **App Client ID** | `53acsgrc0tneq7jhigesj93g8r` |
| **Dominio Cognito** | `https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com` |
| **Redirect URI** | `http://localhost:5173/` |
| **Flujo OAuth2** | Authorization Code Flow (con PKCE) |
| **Scopes Solicitados** | `openid`, `email`, `phone` |

---

## 🚀 Guía de Ejecución

### Prerrequisitos
* **Java:** JDK 21 o superior (se incluye `.sdkmanrc`).
* **Node.js:** Node.js 18+ y npm.

### 1. Backend (`demo/`)

#### Modo Desarrollo (`dev`):
```bash
cd demo
./mvnw spring-boot:run
```
* **API REST:** `http://localhost:8080/api/products`
* **Consola H2 (solo dev):** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, usuario: `sa`, sin contraseña).

#### Modo Producción (`prod`):
```bash
cd demo
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```
*(En producción la consola H2 se encuentra completamente deshabilitada)*.

#### Ejecución de Pruebas Automatizadas:
```bash
cd demo
./mvnw test
```

### 2. Frontend (`frontend/`)

```bash
cd frontend
npm install
npm run dev
```
* **Aplicación:** `http://localhost:5173`

#### Compilación para Producción:
```bash
cd frontend
npm run build
```

---

## 🛡️ Endpoints de la API

| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/products` | Público | Consulta el catálogo completo de productos |
| `GET` | `/api/products/{id}` | Público | Obtiene el detalle de un producto específico |
| `POST` | `/api/products` | **Autenticado (Bearer access_token)** | Registra un nuevo producto (valida datos de entrada) |
| `DELETE` | `/api/products/{id}` | **Autenticado (Bearer access_token)** | Elimina un producto (devuelve 404 si no existe) |

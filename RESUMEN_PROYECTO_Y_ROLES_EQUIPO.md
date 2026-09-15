# 🏛️ DOSSIER TÉCNICO DE ARQUITECTURA, PATRONES, SEGURIDAD Y DISTRIBUCIÓN DE ROLES

> **Proyecto:** Soluciones Cloud v1  
> **Asignatura:** Desarrollo Cloud Native I (DSY1107) — Escuela de Informática y Telecomunicaciones, Duoc UC  
> **Integrantes:** **Ignacio Salazar** • **Cristofer Cifuentes**  
> **Fecha de Consolidación:** Septiembre 2026  
> **Repositorio Oficial:** `ignSf/soluciones_cloud_v1`  
> **Topología en Producción:**
> * 🌐 **Frontend SPA (Vercel):** [https://soluciones-cloud-v1.vercel.app](https://soluciones-cloud-v1.vercel.app)
> * ⚙️ **Backend REST BFF (Render):** [https://soluciones-cloud-v1.onrender.com](https://soluciones-cloud-v1.onrender.com)
> * 🛡️ **Perímetro de Red (Amazon API Gateway):** [https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod](https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod)
> * 🔐 **Proveedores IDaaS:** AWS Cognito (`us-east-1`) y Microsoft Entra ID (Azure AD Tenant)
> * 🗄️ **Persistencia Administrada:** PostgreSQL 15+ en Supabase Cloud (Session Pooler Puerto 5432)

---

## 📑 ÍNDICE DE CONTENIDOS

1. [Arquitecturas del Sistema](#1-arquitecturas-del-sistema)
2. [Patrones de Diseño y de Software Utilizados](#2-patrones-de-diseño-y-de-software-utilizados)
3. [Matriz de Ciberseguridad y Políticas Aplicadas](#3-matriz-de-ciberseguridad-y-políticas-aplicadas)
4. [Ficha Técnica del Stack Tecnológico](#4-ficha-técnica-del-stack-tecnológico)
5. [Módulos de Código y Anatomía de Archivos](#5-módulos-de-código-y-anatomía-de-archivos)
6. [Catálogo de Servicios y Contratos de API (Endpoints)](#6-catálogo-de-servicios-y-contratos-de-api-endpoints)
7. [Distribución Exhaustiva de Responsabilidades (Ignacio vs. Cristofer)](#7-distribución-exhaustiva-de-responsabilidades-ignacio-vs-cristofer)
8. [Trazabilidad de Control de Versiones (Evidencia Git)](#8-trazabilidad-de-control-de-versiones-evidencia-git)

---

## 1. ARQUITECTURAS DEL SISTEMA

El sistema fue diseñado superando el paradigma monolítico tradicional mediante la convergencia de cinco estilos arquitectónicos modernos:

```
                                  ARQUITECTURA GENERAL MULTI-CLOUD
                                  
 [ Navegador Web / Cliente Final ]
                │
                ├─── (1) Descarga de Assets SPA (HTML/JS/CSS)
                ▼
      ┌──────────────────┐
      │   VERCEL EDGE    │ ─── React 19 + TypeScript + Vite
      └──────────────────┘
         │            │
         │ (2) Flujo  │ (3) Autenticación Federada
         │   PKCE     │     (Hosted UI / MSAL Pop-up)
         ▼            ▼
  ┌──────────────┐  ┌───────────────────────┐
  │ AWS COGNITO  │  │  MICROSOFT ENTRA ID   │ ─── Proveedores IDaaS
  └──────────────┘  └───────────────────────┘     (Emisión de JWT con RS256)
         │                     │
         └──────────┬──────────┘
                    │ (4) Envío de Petición HTTP
                    │     Headers: 'Authorization: Bearer <JWT>'
                    ▼
      ┌───────────────────────────┐
      │   AMAZON API GATEWAY      │ ─── Capa Perimetral
      │  (HTTP Proxy + CORS +     │     (Cognito Authorizer en us-east-1)
      │   Filtrado Perimetral)    │
      └───────────────────────────┘
                    │
                    │ (5) Proxy Reverso seguro hacia Backend
                    ▼
      ┌───────────────────────────┐
      │       RENDER CLOUD        │ ─── Backend BFF (Spring Boot 3.4 / Java 17)
      │  ┌─────────────────────┐  │     • Spring Security 6 Resource Server
      │  │  Controller Layer   │  │     • Validación asimétrica con JWKS
      │  ├─────────────────────┤  │     • Stateless Session Management
      │  │   Service Layer     │  │
      │  ├─────────────────────┤  │
      │  │  Repository Layer   │  │
      │  └─────────────────────┘  │
      └───────────────────────────┘
                    │
                    │ (6) Conexión JDBC SSL Cifrada (HikariCP)
                    │     Puerto 5432 (Session Pooler)
                    ▼
      ┌───────────────────────────┐
      │      SUPABASE CLOUD       │ ─── Persistencia Administrada
      │   PostgreSQL 15+ Cluster  │     (Tablas: products, orders)
      └───────────────────────────┘
```

### 1.1 Arquitectura Cloud Native (Nativa de la Nube)
* **Aislamiento de Carga y Micro-contenedores:** La solución prescinde de servidores on-premise o máquinas virtuales monolíticas autogestionadas.
* **Componentes Servicializados (SaaS / PaaS / IaaS):** Cada capa es operada como un servicio administrado con escalamiento automático, alta disponibilidad inherente y acuerdos de nivel de servicio (SLA) de nivel corporativo.

### 1.2 Topología Multi-Cloud Desacoplada
Para mitigar el riesgo de dependencia de un único proveedor (*Vendor Lock-in*) y optimizar el rendimiento, el sistema distribuye sus capacidades entre cuatro plataformas:
* **Vercel:** Despliegue en red de entrega perimetral (Edge Network) para la SPA estática.
* **Amazon Web Services (AWS):** Gestión de identidades con AWS Cognito y filtrado perimetral con Amazon API Gateway en la región `us-east-1` (Norte de Virginia).
* **Microsoft Azure:** Directorio activo en la nube mediante Microsoft Entra ID para la asignación de roles corporativos.
* **Render:** Entorno PaaS contenedorizado (Docker) para la ejecución continua del backend Java.
* **Supabase:** Clúster relacional gestionado de PostgreSQL optimizado para transacciones ACID.

### 1.3 Patrón BFF (Backend for Frontend)
El backend en Spring Boot actúa como una puerta de enlace dedicada para la aplicación cliente. Centraliza el procesamiento de reglas de negocio, la validación de integridad, la orquestación transaccional y la extracción de identidad antes de acceder al almacenamiento relacional.

### 1.4 Modelo Zero Trust (Confianza Cero)
Bajo este principio de ciberseguridad:
* Ningún componente asume que la petición es legítima por el mero hecho de viajar en la red.
* El frontend valida al usuario ante el IdP.
* El API Gateway intercepta y valida el token en el perímetro.
* El backend en Spring Boot no confía ciegamente en el Gateway y vuelve a verificar criptográficamente la firma digital y los claims del JWT contra los certificados públicos oficiales de la nube emisora.
* La base de datos en Supabase restringe su acceso únicamente a conexiones autenticadas con TLS/SSL obligatoria.

### 1.5 Arquitectura 100% Stateless (Sin Estado)
* El servidor en Render no almacena objetos `HttpSession`, no retiene credenciales en variables estáticas y no genera cookies de sesión.
* Cada llamada HTTP es autónoma, transportando en su cabecera `Authorization` la prueba criptográfica completa de su identidad y privilegios, posibilitando el escalamiento horizontal inmediato del backend mediante réplicas sin necesidad de sincronización de memoria compartida o sticky sessions.

---

## 2. PATRONES DE DISEÑO Y DE SOFTWARE UTILIZADOS

### 2.1 Patrones en el Backend (Spring Boot 3 & Java 17)

| Patrón | Implementación en Código | Propósito Técnico |
|---|---|---|
| **Layered Architecture (Arquitectura en Capas)** | `controller` ➔ `service` ➔ `repository` ➔ `model` | Separación estricta de responsabilidades: desacoplar la exposición de APIs HTTP, la lógica de negocio pura, la persistencia y las entidades de datos. |
| **Repository Pattern** | `ProductRepository.java`<br>`OrderRepository.java` | Abstracción de acceso a datos provista por Spring Data JPA; desacopla el modelo de dominio de las sentencias SQL nativas (`SELECT`, `INSERT`). |
| **Inversion of Control (IoC) & Dependency Injection (DI)** | `@RequiredArgsConstructor` (Lombok) + anotaciones `@Service`, `@RestController` | Gestión declarativa del ciclo de vida de los componentes mediante el contenedor de inversión de control de Spring; inyección por constructor para inmutabilidad y pruebas unitarias. |
| **OAuth2 Resource Server Pattern** | `SecurityConfig.java` (`.oauth2ResourceServer()`) | Configuración del servidor no como emisor de claves, sino como validador asimétrico de tokens criptográficos emitidos por un IdP externo (RFC 6749 / 6750). |
| **Identity Principal Injection Pattern** | `@AuthenticationPrincipal Jwt jwt` en controladores | Inyección directa y segura del objeto criptográfico verificado por el filtro de seguridad en los argumentos del método de controlador. |
| **Callback & Lifecycle Hook Pattern** | Método `@PrePersist` en `Order.java` | Ejecución determinista de lógica antes de que JPA persista el registro: estampado automático de la marca temporal (`createdAt`), estado por defecto (`CONFIRMED`) y cálculo de monto (`totalAmount = unitPrice * quantity`). |
| **Data Initializer / Seed Pattern** | `DataInitializer.java` implementando `CommandLineRunner` | Comprobación y siembra inicial idempotente de datos de prueba en la base de datos al arrancar el servidor si el repositorio está vacío (`count() == 0`). |
| **Connection Pooling Pattern** | HikariCP integrado en `application.properties` | Administración de un grupo reutilizable de conexiones persistentes a PostgreSQL, mitigando el coste de handshakes TCP/TLS continuos. |
| **Role-Based Access Control (RBAC)** | `JwtAuthenticationConverter` en `SecurityConfig.java` | Mapeo y traducción del claim de array `roles` emitido por Microsoft Entra ID al prefijo estándar `ROLE_` de Spring Security para aplicar restricciones granulares (`hasRole("Admin")`). |

### 2.2 Patrones en el Frontend (React 19 + TypeScript + Vite)

| Patrón | Implementación en Código | Propósito Técnico |
|---|---|---|
| **Component-Based Architecture** | `Navbar`, `ProductList`, `ProductCard`, `ProductForm` | Descomposición de la interfaz en unidades funcionales aisladas, desacopladas, autocontenidas y con responsabilidades visuales únicas. |
| **Custom Hook Pattern** | `useAuth.ts` | Encapsulación integral de la lógica de autenticación (MSAL / Cognito), gestión del token, captura de usuario y métodos de login/logout en un hook reutilizable. |
| **Service Layer / API Adapter** | `productService.ts`<br>`orderService.ts` | Aislamiento de la lógica de red y del cliente HTTP (`fetch`); encapsula URLs base, inyección de headers `Authorization` y transformación de DTOs. |
| **Strict Type Synchronization** | `types/product.ts`<br>`types/order.ts` | Modelado de contratos de datos fuertemente tipados con TypeScript, sincronizados con las clases del backend para garantizar detección de discrepancias en tiempo de compilación. |
| **Single Page Application (SPA) State Machine** | `App.tsx` (`useState`, `useEffect`) | Manejo reactivo del estado local para navegación de vistas, recarga en vivo de productos y banners de retroalimentación sin recargar la página web. |

### 2.3 Patrones de Seguridad y Red

| Patrón | Implementación | Propósito Técnico |
|---|---|---|
| **Proof Key for Code Exchange (PKCE)** | RFC 7636 en cliente web | Generación de un secreto al vuelo (`code_verifier`) y su resumen criptográfico (`code_challenge`) para evitar la intercepción del código de autorización en clientes públicos sin capacidad de ocultar un secret. |
| **Asymmetric Digital Signature (RS256)** | Clave privada en AWS/Azure y Claves públicas en JWKS | Firma digital con par de claves asimétricas RSA de 2048 bits; el backend comprueba matemáticamente la firma sin poseer jamás la clave privada del emisor. |
| **Defense-in-Depth (Defensa en Profundidad)** | Doble aduana: Amazon API Gateway + Spring Security | Si un atacante burla o elude una capa perimetral, la capa de seguridad interna en el backend neutraliza de forma autónoma la intrusión. |
| **Multiplexed Session Pooling** | Supabase Session Pooler (Puerto 5432) | Multiplexa cientos de conexiones dinámicas provenientes de contenedores en Render sobre un pool fijo de conexiones de PostgreSQL, evitando la saturación de sockets. |

---

## 3. MATRIZ DE CIBERSEGURIDAD Y POLÍTICAS APLICADAS

```
                                  CADENA DE SEGURIDAD CRIPTOGRÁFICA
                                  
 1. NAVEGADOR            2. IDaaS CLOUD          3. PERÍMETRO              4. BACKEND SPRING BOOT
 [Genera PKCE] ────────► [Autentica Credencial] ──► [Amazon API Gateway] ──► [Spring Security 6]
 code_verifier            Hosted UI / MSAL         Cognito Authorizer        Resource Server
 SHA-256 challenge        Emite Token JWT          Bloqueo HTTP 401          Valida firma con JWKS
                          Firmado con RS256        en el borde               Verifica iss, exp y roles
```

### 3.1 Delegación Integral de Identidad (Zero Password Storage)
* Ninguna entidad del código fuente, controlador, script SQL o base de datos en Supabase almacena nombres de usuario, hashes bcrypt o contraseñas.
* Toda la autenticación es delegada a infraestructuras auditadas bajo normas **SOC 2, ISO 27001 y PCI-DSS** (AWS Cognito y Microsoft Entra ID).

### 3.2 Estándar JSON Web Token (RFC 7519)
Cada token generado contiene tres secciones codificadas en Base64URL:
1. **Header:** Especifica el algoritmo (`"alg": "RS256"`), el tipo (`"typ": "JWT"`) y el identificador de la clave pública en el almacén del IdP (`"kid": "..."`).
2. **Payload (Claims de Seguridad):**
   * `sub` *(Subject):* Identificador único inmutable (UUID) del usuario.
   * `email` o `preferred_username`: Dirección de correo verificada.
   * `iss` *(Issuer):* Emisor autoritativo (ej. `https://cognito-idp.us-east-1.amazonaws.com/...` o `sts.windows.net/...`).
   * `exp` *(Expiration):* Timestamp exacto de vigencia; expirado el plazo, el token es rechazado automáticamente.
   * `roles`: Lista de roles corporativos asignados (ej. `["Admin"]`).
3. **Signature:** Firma matemática asimétrica generada con la clave privada de AWS o Microsoft mediante el algoritmo SHA-256 con RSA (`RSASHA256`).

### 3.3 Validación Asimétrica por JWKS (JSON Web Key Sets)
* Spring Boot descarga y almacena en caché de forma periódica el conjunto de certificados públicos expuestos por el IdP en su endpoint JWKS.
* La verificación de validez, procedencia y firma digital se efectúa en microsegundos dentro de la memoria del servidor mediante operaciones matemáticas, **sin requerir llamadas HTTP sincrónicas al proveedor en cada petición ni consultas a la base de datos**.

### 3.4 Prevención de Suplantación de Identidad (Identity Spoofing Prevention)
* En el endpoint transaccional `POST /api/orders`:
  * El cuerpo JSON recibido desde el cliente **no define ni confía en el email del comprador**.
  * El controlador extrae el correo mediante `jwt.getClaimAsString("email")` directamente del payload del token verificado por Spring Security.
  * Resulta matemáticamente imposible para un usuario autenticado emitir compras a nombre de otra persona.

### 3.5 Control de Acceso Granular Basado en Roles (RBAC)
* Endpoints de lectura (`GET /api/products`, `GET /api/products/{id}`): **Públicos (`permitAll`)** para visitantes anónimos.
* Endpoints transaccionales (`POST /api/orders`, `GET /api/orders/my-orders`): **Requieren autenticación obligatoria (`authenticated()`)**.
* Endpoints administrativos (`POST /api/products`, `DELETE /api/products/{id}`): **Restringidos al rol administrador (`hasRole("Admin")`)**, verificado mediante los claims de Microsoft Entra ID.

### 3.6 Blindaje de Red y Superficie de Exposición
* **Desactivación Justificada de CSRF:** Al no almacenar sesiones en cookies del navegador (`SessionCreationPolicy.STATELESS`), las peticiones REST no son vulnerables a Cross-Site Request Forgery, permitiendo desactivar de forma segura el filtro CSRF sin comprometer la seguridad.
* **CORS Estricto:** `SecurityConfig` restringe los orígenes, cabeceras (`Authorization`, `Content-Type`) y métodos permitidos.
* **Cifrado en Tránsito:** Todo el tráfico (Cliente ➔ Vercel ➔ Gateway ➔ Render ➔ Supabase) está cifrado mediante **TLS 1.3 / HTTPS** y **SSL Mode** en PostgreSQL.
* **Variables de Entorno Secretas:** Cero tokens, contraseñas de BD o IDs de cliente incrustados en código duro. Se inyectan en tiempo de ejecución en Render y Vercel.

---

## 4. FICHA TÉCNICA DEL STACK TECNOLÓGICO

### 4.1 Frontend
* **Core:** React 19.0.0
* **Lenguaje:** TypeScript 5.7+
* **Empaquetador & Dev Server:** Vite 6.0+
* **Librerías de Identidad:**
  * `@azure/msal-browser` (^4.5.0): Librería oficial de Microsoft para autenticación en aplicaciones SPA.
  * `@azure/msal-react` (^3.0.0): Envoltorios de contexto y componentes reactivos para React.
* **Estilizado:** CSS Vanilla Moderno con variables CSS, animaciones GPU y diseño responsivo.
* **Alojamiento:** Vercel Edge Platform.

### 4.2 Backend
* **Plataforma:** Java 17 LTS (Eclipse Temurin)
* **Framework:** Spring Boot 3.4.3
* **Seguridad:** Spring Security 6.4+ (`spring-boot-starter-oauth2-resource-server`)
* **Acceso a Datos:** Spring Data JPA / Hibernate ORM
* **Driver Relacional:** PostgreSQL JDBC Driver (`org.postgresql:postgresql`)
* **Utilidades:** Project Lombok 1.18+ (Generación de getters, setters, constructores y builders)
* **Base de Datos de Pruebas:** H2 In-Memory Database (Activación en perfil local)
* **Gestor de Dependencias y Construcción:** Apache Maven 3.9+

### 4.3 Servicios Cloud e Infraestructura
* **IDaaS (AWS):** AWS Cognito User Pools (Dominio Hosted UI, Client ID público, flujo PKCE sin secret).
* **IDaaS (Azure):** Microsoft Entra ID (App Registration con soporte SPA y manifiesto de `appRoles`).
* **API Gateway:** Amazon API Gateway (Protocolo HTTP API / REST con integración HTTP Proxy a Render).
* **Cómputo Backend:** Render Cloud Platform (Despliegue automatizado por Webhook sobre Docker).
* **Contenedor:** Dockerfile Multi-stage (`maven:3.9.9-eclipse-temurin-17` para build y `eclipse-temurin:17-jre-jammy` para ejecución).
* **Base de Datos Cloud:** Clúster Supabase PostgreSQL 15+ con pooling transaccional activo (puerto 5432).

---

## 5. MÓDULOS DE CÓDIGO Y ANATOMÍA DE ARCHIVOS

```
demo/
├── Dockerfile                                 # Empaquetado ligero multi-stage para Render
├── pom.xml                                    # Dependencias Maven (Spring Boot 3, OAuth2, JPA, Postgres)
└── src/main/
    ├── java/com/example/demo/
    │   ├── DemoApplication.java               # Punto de entrada y bootstrap de Spring Boot
    │   ├── config/
    │   │   ├── SecurityConfig.java            # Cadena de filtros de seguridad, CORS, Stateless y RBAC
    │   │   └── DataInitializer.java           # Población idempotente de datos semilla en arranque
    │   ├── controller/
    │   │   ├── ProductController.java         # Endpoints de consulta y administración de productos
    │   │   └── OrderController.java           # Endpoints de pedidos con extracción segura de identidad
    │   ├── model/
    │   │   ├── Product.java                   # Entidad JPA: id, name, price, description, category
    │   │   └── Order.java                     # Entidad JPA: id, userEmail, productId, totalAmount, @PrePersist
    │   ├── repository/
    │   │   ├── ProductRepository.java         # Interfaz Spring Data JPA para catálogo
    │   │   └── OrderRepository.java           # Consultas derivadas (findByUserEmailOrderByCreatedAtDesc)
    │   └── service/
    │       ├── ProductService.java            # Lógica transaccional de productos
    │       └── OrderService.java              # Lógica transaccional y cálculo de órdenes
    └── resources/
        └── application.properties             # Configuración de BD Supabase, JWKS y dialectos JPA

frontend/
├── package.json                               # Dependencias npm (React 19, MSAL, Vite, TypeScript)
├── vite.config.ts                             # Configuración de compilación y servidor local
└── src/
    ├── main.tsx                               # Inicialización de React y montaje en DOM
    ├── App.tsx                                # Orquestador principal de estado, vistas y modales
    ├── index.css                              # Sistema de diseño, variables CSS y animaciones
    ├── auth/
    │   ├── authConfig.ts                      # Parámetros de conexión con AWS Cognito y PKCE
    │   ├── msalConfig.ts                      # Configuración del PublicClientApplication de Entra ID
    │   ├── loginRequest.ts                    # Definición de scopes solicitados (openid, profile, email)
    │   └── useAuth.ts                         # Hook personalizado que unifica la autenticación dual
    ├── components/
    │   ├── Navbar.tsx                         # Barra superior reactiva al estado de autenticación
    │   ├── ProductList.tsx                    # Grilla responsiva de tarjetas de productos
    │   ├── ProductCard.tsx                    # Tarjeta unitaria con botón de compra interactivo
    │   └── ProductForm.tsx                    # Formulario de alta de productos para administradores
    ├── services/
    │   ├── productService.ts                  # Cliente API para operaciones de productos
    │   └── orderService.ts                    # Cliente API para creación y auditoría de pedidos
    └── types/
        ├── product.ts                         # Interfaz TypeScript del Producto
        └── order.ts                           # Interfaz TypeScript de la Orden
```

---

## 6. CATÁLOGO DE SERVICIOS Y CONTRATOS DE API (ENDPOINTS)

### 6.1 Módulo de Catálogo de Productos (`/api/products`)

| Método | Ruta | Seguridad | Roles | Request Body | Response Status | Descripción |
|---|---|---|---|---|---|---|
| `GET` | `/api/products` | **Público** | Cualquiera | *Ninguno* | `200 OK` | Retorna la lista completa de productos en formato JSON. |
| `GET` | `/api/products/{id}` | **Público** | Cualquiera | *Ninguno* | `200 OK` / `404 Not Found` | Retorna el detalle de un producto por su ID numérico. |
| `POST` | `/api/products` | **Protegido (JWT)** | `ROLE_Admin` | `{ "name": "...", "price": 100, "description": "...", "category": "..." }` | `201 Created` / `403 Forbidden` | Crea un nuevo registro de producto en Supabase. |
| `DELETE` | `/api/products/{id}` | **Protegido (JWT)** | `ROLE_Admin` | *Ninguno* | `204 No Content` / `403 Forbidden` | Elimina permanentemente un producto de la base de datos. |

### 6.2 Módulo de Gestión de Pedidos (`/api/orders`)

| Método | Ruta | Seguridad | Roles | Request Body | Response Status | Descripción |
|---|---|---|---|---|---|---|
| `POST` | `/api/orders` | **Protegido (JWT)** | Autenticado | `{ "productId": 1, "productName": "...", "unitPrice": 50.0, "quantity": 2 }` | `201 Created` / `401 Unauthorized` | Registra una nueva orden asignando el `userEmail` del token verificado. |
| `GET` | `/api/orders/my-orders` | **Protegido (JWT)** | Autenticado | *Ninguno* | `200 OK` / `401 Unauthorized` | Retorna el historial de compras del usuario autenticado ordenado por fecha. |
| `GET` | `/api/orders` | **Protegido (JWT)** | Autenticado | *Ninguno* | `200 OK` | Consulta administrativa de todas las órdenes del sistema. |

---

## 7. DISTRIBUCIÓN EXHAUSTIVA DE RESPONSABILIDADES (IGNACIO VS. CRISTOFER)

Para brindar un respaldo sólido e irrebatible ante la comisión examinadora, las responsabilidades se desglosan en detalle a continuación:

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                      MATRIZ DE DIVISIÓN DE RESPONSABILIDADES                     │
├─────────────────────────────────────────┬────────────────────────────────────────┤
│     IGNACIO SALAZAR (Líder / Fullstack) │   CRISTOFER CIFUENTES (Cloud Identity) │
├─────────────────────────────────────────┼────────────────────────────────────────┤
│ • Arquitectura Backend en Spring Boot 3 │ • Integración de Microsoft Entra ID    │
│ • Resource Server base con JWKS         │ • Rama de desarrollo 'rama-entraid'    │
│ • Persistencia Supabase + HikariCP      │ • Librerías @azure/msal-browser        │
│ • Infraestructura Docker y Render       │ • Hook useAuth.ts & msalConfig.ts      │
│ • Despliegue Frontend en Vercel         │ • Configuración de appRoles en Azure   │
│ • Enlace con Amazon API Gateway         │ • Control RBAC en SecurityConfig.java  │
│ • Microservicio Orders (/api/orders)    │ • Sincronización de sesión en UI       │
│ • Desarrollo UI/UX SPA en React 19      │                                        │
│ • Soporte de Autenticación Dual         │                                        │
│ • Suite Documental e Informes Duoc UC   │                                        │
└─────────────────────────────────────────┴────────────────────────────────────────┘
```

### 👤 7.1 Aportes y Módulos Desarrollados por Ignacio Salazar (Tú)
**Rol Técnico:** *Líder de Arquitectura, Ingeniero Backend/Frontend & DevOps*

1. **Cimiento del Backend & Resource Server:**
   * Creación del proyecto base en **Spring Boot 3.4** con Java 17 y configuración del `pom.xml`.
   * Implementación de la arquitectura desacoplada en 4 capas (`controller`, `service`, `repository`, `model`).
   * Configuración de **Spring Security 6** como Resource Server para validación de tokens JWT mediante firmas asimétricas RS256 contra JWKS.
2. **Microservicio de Pedidos (`/api/orders`):**
   * Diseño de la entidad `Order.java` incorporando el callback de ciclo de vida `@PrePersist` para cómputo de totales en servidor y sellado de fecha de creación.
   * Programación de `OrderController.java` y `OrderService.java` con lógica de extracción de identidad segura (`@AuthenticationPrincipal Jwt`) para evitar suplantación de identidad.
3. **Persistencia Cloud en Supabase (PostgreSQL 15+):**
   * Configuración de la base de datos en Supabase, diseño de tablas e índices (`supabase_schema.sql`).
   * Configuración del pool de conexiones **HikariCP** conectando al **Session Pooler (puerto 5432)** para resolver la concurrencia en la nube.
4. **DevOps, Contenedores & Despliegue Multi-Cloud:**
   * Creación del `Dockerfile` multi-stage para compilar con Maven y empaquetar una imagen JRE Jammy ultra-liviana.
   * Configuración y despliegue del servicio web en **Render** con inyección de variables de entorno y soporte para el puerto dinámico asignado por el PaaS.
   * Despliegue del cliente web en **Vercel** y configuración de DNS/SSL.
   * Conexión perimetral de **Amazon API Gateway** (us-east-1) como proxy inverso perimetral.
5. **Frontend SPA, UI/UX & Animaciones:**
   * Creación de la aplicación en **React 19 + TypeScript + Vite**.
   * Diseño visual con paleta ejecutiva, tipografías personalizadas y animación de nubes japonesas en el hero de la página principal.
   * Componentes `ProductList`, `ProductCard`, vistas dinámicas del catálogo y panel de "Mis Pedidos".
6. **Orquestación de Autenticación Dual:**
   * Programación de mecanismos de fallback en frontend y backend para permitir que convivan tanto **AWS Cognito** como **Microsoft Entra ID**.
7. **Documentación Oficial de Evaluación y Presentaciones:**
   * Redacción íntegra del informe de evaluación formal (`INFORME_EVALUACION_PARCIAL_1_DSY1107.md`).
   * Elaboración de guías maestras de estudio y defensa técnica.
   * Construcción del generador automático de PowerPoint (`create_presentation.py`) y del visor web ([presentacion.html](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/presentacion.html)).

---

### 👤 7.2 Aportes y Módulos Desarrollados por Cristofer Cifuentes (Compañero)
**Rol Técnico:** *Especialista en Identidad Cloud & Microsoft Entra ID*

1. **Investigación e Integración de Microsoft Entra ID:**
   * Apertura y desarrollo autónomo de la rama de Git `rama-entraid`.
   * Incorporación en el frontend de las librerías oficiales `@azure/msal-browser` y `@azure/msal-react`.
   * Redacción de los archivos base de inicialización del cliente público de Microsoft:
     * `msalConfig.ts`: Configuración del Client ID, autoridad de Azure y almacenamiento en caché.
     * `loginRequest.ts`: Solicitud explícita de scopes estándar (`openid`, `profile`, `email`).
     * `useAuth.ts`: Hook para encapsular la adquisición de tokens silenciosos (`acquireTokenSilent`) y redirección/pop-up de login.
2. **Implementación de Autorización Granular (RBAC con `appRoles`):**
   * Configuración de los roles de aplicación dentro del portal corporativo de Azure (Microsoft Entra ID), definiendo el rol `Admin`.
   * Refactorización de `SecurityConfig.java` en Spring Boot:
     * Creación del bean `jwtAuthenticationConverter()`.
     * Uso de `JwtGrantedAuthoritiesConverter` para extraer el claim `roles` del JWT y añadir el prefijo `ROLE_`.
     * Blindaje de endpoints administrativos en Spring Security: `.requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("Admin")` y `DELETE ... hasRole("Admin")`.
3. **Sincronización del Estado de Usuario en Componentes de Frontend:**
   * Adaptación de `Navbar.tsx` para consumir el estado del hook `useAuth`, permitiendo renderizar el nombre del usuario y el botón de cierre de sesión según la cuenta de Microsoft activa.

---

## 8. TRAZABILIDAD DE CONTROL DE VERSIONES (EVIDENCIA GIT)

Los siguientes commits registran cronológicamente las contribuciones de cada integrante en el repositorio:

| Hash de Commit | Autor Registrado | Líneas Modificadas | Resumen del Aporte Técnico |
|---|---|---|---|
| `cd260e8` | **Cristofer Cifuentes** | +153 / -88 | Creación de `msalConfig.ts`, `useAuth.ts`, dependencias MSAL en `package.json` y migración hacia Entra ID. |
| `0abd33c` | **Cristofer Cifuentes** | +18 / -4 | Implementación de `jwtAuthenticationConverter` y autorización `hasRole("Admin")` en `SecurityConfig.java`. |
| `7bfb8d6` | **Cristofer Cifuentes** | +19 / -13 | Ajustes de propagación de sesión en `Navbar.tsx` y `App.tsx`. |
| `532ec27` | **Ignacio Salazar** | Merge | `Merge branch 'rama-entraid' into tests-to-main`: Fusión de la rama de Entra ID al tronco principal. |
| `18f5e0e` | **Ignacio Salazar** | Core Auth | Integración de soporte dual para que la app funcione con Cognito o Entra ID sin quiebres. |
| `81ace9c` | **Ignacio Salazar** | Config | Depuración de scopes de acceso en MSAL y ajustes de fallback. |
| `cd71479` | **Ignacio Salazar** | Production Fix | Valores de respaldo de Entra ID para entorno en vivo en Vercel. |
| `5e8cffb` | **Ignacio Salazar** | Backend & UI | Creación del microservicio de órdenes (`OrderController`, `OrderService`, `OrderRepository`, vista frontend). |
| `e178da5` | **Ignacio Salazar** | Gateway | Enlace del frontend con Amazon API Gateway y pruebas de endpoints proxy. |
| `35b13b2` | **Ignacio Salazar** | Deploy | Conexión con backend desplegado en Render y `redirect_uri` dinámico. |
| `086561c` | **Ignacio Salazar** | DevOps | Creación del `Dockerfile` multi-stage y adaptación de puerto dinámico para Render. |
| `3b60fdc` | **Ignacio Salazar** | Database | Conexión de base de datos Supabase PostgreSQL y script `supabase_schema.sql`. |
| `9f9cb07` | **Ignacio Salazar** | Security Core | Arquitectura inicial de Spring Security OAuth2 Resource Server con JWKS y documentación. |
| *Múltiples* | **Ignacio Salazar** | UI / Docs | Maquetación visual completa (animaciones, componentes CSS) e informes académicos Duoc UC. |

---

> [!IMPORTANT]
> **Recomendación para la Defensa Oral:**
> Durante la exposición, utilicen este documento como respaldo formal. Demuestra una planificación rigurosa, un diseño de arquitectura de grado industrial y una división del trabajo donde ambos integrantes aportaron código de alto valor técnico y seguridad crítica.

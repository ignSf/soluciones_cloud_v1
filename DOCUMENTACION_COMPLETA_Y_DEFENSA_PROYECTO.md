# Guía Definitiva de Arquitectura, Seguridad y Flujo del Sistema Cloud

> **Proyecto:** Soluciones Cloud v1  
> **Propósito:** Documento de referencia técnica, conceptual y narrativa para la defensa del proyecto ante evaluadores, docentes o equipos de ingeniería.  
> **Infraestructura Desplegada:** Vercel (Frontend), Render (Backend), AWS Cognito (IDaaS) y Supabase (Base de Datos PostgreSQL).

---

## 🌟 PARTE I: EL FLUJO NARRADO EN PALABRAS (Explicación Humana y Sencilla)

*Si tuviera que defender o explicar este proyecto en una presentación oral ante una comisión evaluadora, este es el relato conceptual paso a paso:*

### ¿Qué es este sistema?
Es una plataforma web de catálogo y gestión de productos para comercio electrónico o inventario empresarial, construida bajo el paradigma **Cloud Native** (nativo de la nube), completamente desacoplada y sin servidores locales que mantener.

### ¿Cómo funciona la arquitectura en el día a día?

1. **El usuario llega a la aplicación:**
   * El cliente entra desde su navegador a la dirección de **Vercel** (`https://soluciones-cloud-v1.vercel.app`).
   * La interfaz carga al instante porque son archivos web estáticos y optimizados construidos con **React y Vite**.
   * Inmediatamente, la aplicación hace una consulta pública al backend en **Render** para traer la lista de productos que existen en la base de datos de **Supabase**. Cualquier persona puede ver el catálogo sin estar registrada.

2. **El usuario decide autenticarse (Login):**
   * El usuario hace clic en **"Iniciar sesión con Cognito"**.
   * La aplicación **no le pide la contraseña en una ventanita propia**; en su lugar, lo redirige de forma segura a una pantalla administrada directamente por Amazon Web Services (**AWS Cognito Hosted UI**).
   * Para proteger la comunicación contra hackers, la aplicación en React genera una clave secreta al vuelo llamada `code_verifier` y le aplica un hash matemático para obtener un `code_challenge` (este mecanismo se llama **PKCE**).
   * El usuario ingresa su correo y contraseña directamente en los servidores de Amazon. **Ni Vercel ni Render ven jamás la contraseña del usuario**, lo que garantiza máxima seguridad y privacidad.

3. **Cognito valida y devuelve los tokens:**
   * Amazon confirma que las credenciales son correctas y redirige al navegador de vuelta a Vercel con un código de autorización temporal de un solo uso en la URL.
   * La aplicación en React toma ese código y se lo envía a Cognito junto con la clave secreta generada al principio (`code_verifier`).
   * Amazon comprueba que todo coincide y le entrega a React un paquete con tres llaves digitales (**Tokens JWT**): el `id_token` (datos de quién es el usuario), el `access_token` (permisos del usuario) y el `refresh_token` (para no tener que pedirle la contraseña a cada rato).
   * La pantalla de React se actualiza y muestra el saludo con el correo del usuario y el botón de cerrar sesión.

4. **El usuario crea un nuevo producto:**
   * Con la sesión iniciada, el usuario completa el formulario (nombre, precio, descripción, categoría) y presiona **"Guardar Producto"**.
   * El frontend empaqueta los datos y hace una petición `POST` al servidor en **Render** (`https://soluciones-cloud-v1.onrender.com/api/products`).
   * Pero en la cabecera HTTP de la petición, el frontend adjunta el token JWT como un salvoconducto: `Authorization: Bearer <token>`.

5. **El Backend valida el token y guarda en la base de datos:**
   * La petición llega al servidor **Spring Boot** en Render.
   * Antes de dejar pasar la solicitud al controlador, **Spring Security** intercepta el token.
   * En lugar de preguntar a una base de datos local, Spring Security se conecta a las claves públicas que publica AWS Cognito en Internet (JWKS) y realiza una operación matemática asimétrica (RS256):
     * *¿La firma coincide con la clave de Amazon?* Sí.
     * *¿El token ya expiró?* No.
     * *¿El token fue emitido para nuestro User Pool?* Sí.
   * Una vez superada la prueba de seguridad, Spring Boot toma los datos del producto y, a través de **JPA / Hibernate**, ejecuta una instrucción `INSERT` en la base de datos PostgreSQL alojada en **Supabase** a través de su Connection Pooler.
   * Supabase guarda el registro permanentemente y le asigna un `id` único.

6. **Confirmación en pantalla:**
   * Spring Boot responde con un código `HTTP 201 Created` y el producto en formato JSON.
   * React recibe la confirmación, actualiza la lista en pantalla en tiempo real sin recargar la página y limpia el formulario.

---

## 📋 PARTE II: CUESTIONARIO TÉCNICO DETALLADO

A continuación se responde exhaustivamente a cada una de las preguntas formuladas sobre el sistema:

---

### 1. ¿Cuál es el stack tecnológico usado en la solución?

El proyecto utiliza un stack de última generación separado en tres capas desacopladas:

* **Frontend (Capa de Presentación):**
  * **React 18 + TypeScript:** Biblioteca para interfaces de usuario reactivas con tipado estático riguroso.
  * **Vite:** Herramienta de compilación ultrarrápida para desarrollo frontend.
  * **react-oidc-context & oidc-client-ts:** Implementación certificada del protocolo OpenID Connect (OIDC) y OAuth 2.0 con soporte automático de PKCE.
  * **Vanilla CSS Moderno:** Hojas de estilo con variables personalizadas, diseño responsive y estética dark glassmorphism.
  * **Hosting:** **Vercel** (Edge CDN global con despliegue continuo desde GitHub).

* **Backend (Capa de Lógica y Servidor de Recursos):**
  * **Java 21 LTS:** Entorno de ejecución con alto rendimiento y gestión eficiente de hilos.
  * **Spring Boot 4.x / 3.x:** Framework empresarial para creación de APIs REST.
  * **Spring Security 6 (OAuth2 Resource Server):** Módulo de seguridad nativo para validación declarativa de tokens JWT.
  * **Spring Data JPA / Hibernate:** Capa de persistencia y mapeo objeto-relacional (ORM).
  * **Driver PostgreSQL (`org.postgresql:postgresql`):** Conector JDBC para bases de datos relacionales en la nube.
  * **Docker:** Contenedor multi-stage para empaquetado y ejecución ligera en la nube.
  * **Hosting:** **Render** (Web Service en contenedor Docker enlazado a GitHub).

* **Identidad y Base de Datos (Servicios Cloud):**
  * **AWS Cognito (IDaaS):** User Pool para autenticación, gestión de credenciales, cifrado y Hosted UI.
  * **Supabase (BaaS / Database):** PostgreSQL administrado en la nube con Connection Pooler (puerto 5432).

---

### 2. Explicar la arquitectura del stack tecnológico

La arquitectura responde al patrón **Resource Server / Single Page Application con Proveedor de Identidad Desacoplado**:

```mermaid
graph LR
    A[Navegador del Usuario] -->|1. Carga SPA| B(Vercel: React + Vite)
    A -->|2. Login / Contraseña| C(AWS Cognito IDaaS)
    C -->|3. Entrega Tokens JWT| A
    B -->|4. HTTP + Bearer Token| D(Render: Spring Boot)
    D -->|5. Descarga Claves JWKS| C
    D -->|6. SQL JDBC Session Pooler| E[(Supabase: PostgreSQL)]
```

* **Separación de responsabilidades:** El frontend jamás procesa contraseñas; el backend jamás valida contraseñas (solo verifica firmas criptográficas); la base de datos solo almacena información de negocio (productos), no credenciales de usuarios.
* **Sin estado (Stateless):** El backend no almacena sesiones en memoria (ni sesiones HTTP ni cookies). Cada petición viaja con su propio token firmado. Esto permite escalar el backend horizontalmente en decenas de servidores sin perder la sesión.

---

### 3. ¿Por qué se usa un IDaaS (Identity as a Service)?

Se utiliza un proveedor de identidad como servicio (**AWS Cognito**) por cuatro razones críticas:
1. **Seguridad y Responsabilidad:** Almacenar contraseñas en bases de datos propias expone al negocio a filtraciones, demandas legales y ataques cibernéticos. Con un IDaaS, las credenciales residen en la infraestructura blindada de Amazon Web Services, que cumple normativas como **ISO 27001, SOC 1/2/3, PCI-DSS y GDPR**.
2. **Defensa contra ataques:** Los IDaaS incluyen mitigación automática contra ataques de fuerza bruta (*credential stuffing*), bloqueo de IPs sospechosas, políticas de contraseñas robustas y autenticación multifactor (MFA).
3. **Flujos completos incluidos:** Provee sin costo de desarrollo pantallas de login, registro, recuperación de contraseña por correo electrónico y verificación de email.
4. **Desacoplamiento:** Si el día de mañana se cambia de base de datos o de backend, la base de usuarios y sus identidades permanecen intactas en Cognito.

---

### 4. ¿Por qué se utiliza un API Gateway?

Aunque en esta etapa de desarrollo el frontend se comunica directamente con el Web Service de Render, en una arquitectura de nube empresarial se coloca un **API Gateway** (como AWS API Gateway, Kong o Spring Cloud Gateway) al frente por las siguientes ventajas:
1. **Punto Único de Entrada (Single Entrypoint):** Los clientes solo conocen una URL. El gateway enruta internamente hacia decenas de microservicios sin exponer la arquitectura interna.
2. **Descarga de Seguridad (Offloading de Autenticación):** El API Gateway valida el token JWT en el perímetro de la red. Si el token es falso o expiró, rechaza la petición inmediatamente antes de que consuma memoria o CPU en los servidores de backend.
3. **Rate Limiting y Protección DoS:** Limita la cantidad de peticiones por segundo por usuario o por IP para evitar que saturen la aplicación.
4. **Políticas de CORS unificadas:** Se configuran los orígenes permitidos en un solo lugar y no en cada microservicio individual.

---

### 5. Flujo de trabajo: Desde presionar Login hasta tener la sesión iniciada

El sistema implementa el estándar de la industria: **OAuth 2.0 Authorization Code Flow con PKCE**:

1. **Clic en "Iniciar Sesión":** El usuario hace clic en el botón de la barra de navegación.
2. **Generación del desafío PKCE:** La librería `react-oidc-context` en el frontend genera dos valores criptográficos:
   * `code_verifier`: Una cadena aleatoria secreta de alta entropía.
   * `code_challenge`: El hash matemático `BASE64URL(SHA256(code_verifier))`.
3. **Redirección:** El navegador es redirigido a la Hosted UI de Cognito:
   ```text
   https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com/login?
     client_id=53acsgrc0tneq7jhigesj93g8r&
     response_type=code&
     scope=email+openid+phone&
     redirect_uri=https://soluciones-cloud-v1.vercel.app/&
     code_challenge=xyz...&
     code_challenge_method=S256
   ```
4. **Autenticación:** El usuario introduce sus datos en la página de AWS.
5. **Retorno con Código:** Si es correcto, Cognito redirige al navegador de vuelta a la `redirect_uri` con un parámetro en la URL: `https://soluciones-cloud-v1.vercel.app/?code=abc-123-codigo`.
6. **Canje de Tokens (Token Exchange):** El frontend intercepta el código de la URL y hace un `POST` interno y transparente a `https://...amazoncognito.com/oauth2/token` enviando:
   * El código recibido (`code`).
   * El secreto original previo al hash (`code_verifier`).
7. **Verificación de Amazon:** Cognito aplica SHA-256 al `code_verifier` recibido y comprueba que sea idéntico al `code_challenge` que guardó en el paso 3.
8. **Entrega de Tokens:** Cognito entrega el JSON con el `id_token`, `access_token` y `refresh_token`.
9. **Sesión Activa:** El frontend guarda los tokens en almacenamiento local seguro y actualiza la variable `auth.isAuthenticated = true`.

---

### 6. ¿Qué es el PKCE (Proof Key for Code Exchange)?

**PKCE (RFC 7636)** es una extensión de seguridad para OAuth 2.0 creada para proteger **clientes públicos** (Single Page Applications en React/Angular/Vue y aplicaciones móviles).

* **El Problema:** En el flujo tradicional de OAuth 2.0, el cliente usa un `client_secret` (una contraseña secreta) para canjear el código por tokens. Pero en una aplicación web en el navegador, **el código fuente es público**: cualquier usuario puede abrir la consola de desarrollo y robar el secreto. Sin secreto, un atacante que intercepte el código de autorización en la URL podría canjearlo por tokens y suplantar al usuario.
* **La Solución de PKCE:** Reemplaza el secreto estático por un secreto dinámico generado al vuelo por cada intento de login (`code_verifier`). Al enviar primero el hash (`code_challenge`) y luego el original, solo el navegador que inició el login puede reclamar los tokens, neutralizando ataques de interceptación.

---

### 7. Estructura de un JWT (JSON Web Token)

Un JWT es una cadena de texto dividida en 3 segmentos separados por puntos (`.`):

$$\text{JWT} = \text{Header} . \text{Payload} . \text{Signature}$$

1. **Header (Encabezado):** Indica los metadatos y el algoritmo criptográfico utilizado.
   ```json
   {
     "kid": "clave-publica-cognito-id",
     "alg": "RS256",
     "typ": "JWT"
   }
   ```
2. **Payload (Cuerpo / Claims):** Contiene los datos del usuario y de la sesión (codificados en Base64Url, **no cifrados**; cualquier persona puede leerlos, pero nadie puede alterarlos sin romper la firma).
   ```json
   {
     "sub": "b2685791-7091-7078-4eb7-4a0d84c67923",
     "email": "usuario@correo.com",
     "cognito:groups": ["USER"],
     "iss": "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL",
     "exp": 1725843600
   }
   ```
3. **Signature (Firma Criptográfica):** Es el sello de garantía de autenticidad. Se genera tomando el Header y el Payload codificados y firmándolos con la **clave privada de AWS Cognito** mediante el algoritmo asimétrico **RS256**:
   $$\text{Firma} = \text{CifrarConClavePrivada}(\text{SHA256}(\text{Header} + "." + \text{Payload}))$$

---

### 8. Si tenemos un JWT y alguien modifica o cambia el rol de "USER" a "ADMIN" hardcodeándolo: ¿Dónde se tendría que modificar y qué ocurriría?

* **¿Dónde se modifica?**
  Se modificaría en el **Payload**. Como el payload es solo texto codificado en Base64Url, cualquier usuario puede decodificarlo con herramientas como `jwt.io`, cambiar `"cognito:groups": ["USER"]` por `"cognito:groups": ["ADMIN"]`, volver a codificarlo en Base64Url y armar el nuevo token.

* **¿Qué ocurriría exactamente?**
  **El token será rechazado de inmediato con un error HTTP 401 Unauthorized.**
  * *Razón técnica:* La firma digital adjunta al final del token fue calculada por AWS utilizando su **clave privada** sobre los datos originales.
  * Cuando el backend en Spring Boot recibe la petición con el token alterado, toma la **clave pública** de AWS Cognito (disponible en el endpoint JWKS) y verifica matemáticamente la firma contra el nuevo Header y Payload.
  * Como el hash del contenido alterado no coincide con el valor descifrado de la firma, el algoritmo criptográfico detecta la alteración (`SignatureException` / `BadJwtException`).
  * Como el atacante no tiene la clave privada de AWS Cognito para firmar el nuevo contenido, **es matemáticamente imposible falsificar o alterar un rol sin que el backend lo detecte y lo rechace**.

---

### 9. Diferencia entre IdToken y AccessToken: ¿Cuál debería enviarse al backend?

| Característica | IdToken (`id_token`) | AccessToken (`access_token`) |
| :--- | :--- | :--- |
| **Definido en** | OpenID Connect (OIDC). | OAuth 2.0 (RFC 6749 / 6750). |
| **Propósito** | Informar al **Cliente (Frontend)** **quién** es el usuario (identidad). | Autorizar al **Servidor de Recursos (Backend)** a realizar acciones (autorización). |
| **Contenido principal** | Datos personales: `name`, `email`, `sub`, `email_verified`. | Permisos y privilegios: `client_id`, `scope`, `token_use=access`. |
| **Audiencia (`aud`)** | El ID del cliente frontend (`client_id`). | No tiene claim `aud` por defecto en Cognito, sino `client_id`. |

* **¿Cuál debe enviarse al backend?**
  * **La regla técnica oficial:** Para autorizar llamadas a una API protegida se debe enviar el **`access_token`**.
  * **La excepción práctica con Cognito:** Si el backend necesita saber directamente el correo electrónico o nombre del usuario sin hacer una segunda llamada de red al endpoint `/oauth2/userInfo` de AWS, se permite enviar el **`id_token`**, siempre que el backend valide estrictamente la firma criptográfica y el emisor (`iss`).

---

### 10. ¿Cómo se hicieron las implementaciones en el código y cómo se valida el token?

El backend atravesó dos etapas evolutivas:

1. **Implementación Manual Inicial (Nimbus JOSE):**
   * Se creó un filtro propio `JwtAuthenticationFilter` que interceptaba cada petición HTTP.
   * Se creó `JwtValidationService` con la librería Nimbus, que descargaba manualmente las claves de AWS con `RemoteJWKSet`, comparaba la fecha de expiración y el emisor con condicionales `if (expirationTime.before(now))`.
2. **Implementación Nativa Moderna (Spring Security OAuth2 Resource Server):**
   * Se reemplazaron ambas clases manuales por el starter oficial: `spring-boot-starter-oauth2-resource-server`.
   * En `application.properties` se configuró una sola directiva estándar:
     ```properties
     spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL
     ```
   * En `SecurityConfig.java` se declaró la protección nativa:
     ```java
     .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
     ```

#### Flujo Lógico de Validación en el Backend:
1. El filtro `BearerTokenAuthenticationFilter` extrae el token de la cabecera `Authorization: Bearer <token>`.
2. Consulta dinámicamente `/.well-known/openid-configuration` de Cognito para descargar y cachear las claves públicas (`jwks.json`).
3. Comprueba mediante **RS256** que la firma fue creada por la clave privada de AWS.
4. Verifica que `iss` sea exactamente la URL del User Pool configurado.
5. Verifica que `exp` (expiración) sea superior a la hora actual del servidor.
6. Si todo es válido, crea un `JwtAuthenticationToken` y lo inyecta en el `SecurityContextHolder`.

---

### 11. ¿Dónde está la configuración de CORS y por qué es necesaria?

CORS (*Cross-Origin Resource Sharing*) es un mecanismo de seguridad de los navegadores que bloquea llamadas JavaScript hechas desde un dominio (ej. `https://soluciones-cloud-v1.vercel.app`) hacia otro dominio distinto (ej. `https://soluciones-cloud-v1.onrender.com`).

En nuestro backend, la configuración está centralizada en **[SecurityConfig.java](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/demo/src/main/java/com/example/demo/config/SecurityConfig.java)**:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*")); // Permite peticiones desde el frontend en Vercel
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```
* Permite las peticiones preliminares de sondeo **`OPTIONS` (Preflight)** sin pedir autenticación.
* Permite que viaje la cabecera sensible **`Authorization`** con el token Bearer.

---

### 12. ¿Qué es lo que se tiene que configurar en los IDaaS para que se conecte correctamente con el Frontend y el Backend?

En la consola de **AWS Cognito** se configuraron los siguientes componentes obligatorios:

1. **User Pool (Grupo de Usuarios):**
   * Configuración de atributos requeridos (email verificado).
   * Definición de políticas de robustez de contraseña.
2. **App Client (Cliente de Aplicación):**
   * **Tipo Público:** Sin *Client Secret* (esencial para aplicaciones React).
   * **Flujos OAuth 2.0:** Marcado **Concesión de código de autorización (Authorization Code Grant)** con soporte forzado de PKCE.
   * **Scopes:** `email`, `openid`, `phone`.
3. **Páginas de Inicio de Sesión Administradas (Hosted UI):**
   * **Allowed callback URLs (URLs de retorno):**
     * `https://soluciones-cloud-v1.vercel.app/` *(para producción en Vercel)*.
     * `http://localhost:5173/` *(para desarrollo local)*.
   * **Allowed sign-out URLs (URLs de cierre de sesión):**
     * `https://soluciones-cloud-v1.vercel.app/`
     * `http://localhost:5173/`
   * **Dominio de Cognito:** Creación de un subdominio único (ej. `https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com`).

---

### 13. Explicar el flujo de la aplicación desde que se hace clic en "Agregar Producto"

1. **Acción en la interfaz:** El usuario llena los campos en `ProductForm.tsx` (Nombre, Precio, Descripción, Categoría) y presiona **"Guardar Producto"**.
2. **Verificación local:** En `App.tsx`, el manejador `handleCreateProduct` verifica:
   ```typescript
   if (!auth.isAuthenticated) {
     alert('⚠️ Debe iniciar sesión para crear productos.');
     return;
   }
   ```
3. **Petición HTTP:** Se invoca `productService.create(product, userToken)`, enviando una petición `POST` con la cabecera:
   ```http
   POST /api/products HTTP/1.1
   Host: soluciones-cloud-v1.onrender.com
   Content-Type: application/json
   Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
   ```
4. **Filtro de Seguridad en Render:** Spring Security intercepta la petición, verifica la firma criptográfica con las claves JWKS de AWS Cognito y confirma la validez del token.
5. **Controlador:** Se ejecuta el método `createProduct` en `ProductController.java`.
6. **Servicio y Persistencia:** `ProductService.java` llama a `ProductRepository.save(product)`.
7. **Base de datos Supabase:** Hibernate emite un `INSERT INTO products ...` hacia la base de datos PostgreSQL conectada mediante el **Session Pooler** (puerto 5432).
8. **Respuesta:** Render responde con `HTTP 201 Created` y el JSON del producto recién guardado.
9. **Actualización Reactiva:** React recibe la respuesta y actualiza el estado local (`setProducts(prev => [...prev, created])`), lo que hace que la tarjeta del nuevo producto aparezca instantáneamente en la pantalla del usuario.

---

## 🎯 RESUMEN DE URLs Y CREDENCIALES DEL SISTEMA

| Componente | Plataforma | URL / Identificador |
| :--- | :--- | :--- |
| **Frontend Web** | Vercel | `https://soluciones-cloud-v1.vercel.app` |
| **Backend REST API** | Render | `https://soluciones-cloud-v1.onrender.com` |
| **Código Fuente** | GitHub | `https://github.com/ignSf/soluciones_cloud_v1` |
| **User Pool ID** | AWS Cognito | `us-east-1_OL9DjB9XL` |
| **App Client ID** | AWS Cognito | `53acsgrc0tneq7jhigesj93g8r` |
| **Dominio Auth** | AWS Cognito | `https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com` |
| **Base de Datos** | Supabase | PostgreSQL en `aws-0-us-east-1.pooler.supabase.com:5432` |

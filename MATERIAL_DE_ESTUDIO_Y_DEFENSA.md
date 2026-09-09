# Material Maestro de Estudio y Defensa: Arquitectura Cloud, Seguridad y Flujo JWT

> **Proyecto:** Soluciones Cloud v1  
> **Servicios Desplegados:** Vercel (Frontend) · Render (Backend) · AWS Cognito (IDaaS) · Supabase (PostgreSQL)  
> **Propósito:** Guía de estudio integral, detallada y narrativa para la defensa y examen del proyecto ante profesores, evaluadores o equipos técnicos.

---

# 🎙️ MÓDULO 1: EL FLUJO NARRADO EN PALABRAS (Guion para Exposición Oral)

*Este módulo está redactado como un discurso continuo y fluido. Si un profesor o evaluador le dice: **"Explícame cómo funciona todo tu proyecto de principio a fin con tus propias palabras"**, este es el relato exacto que debe exponer:*

---

### 1. La Entrada al Ecosistema (Frontend en Vercel)
"Nuestro sistema es una plataforma web moderna para la gestión de productos, diseñada bajo el principio de **desacoplamiento total y arquitectura sin estado (*Stateless*)**.

Todo comienza cuando el usuario ingresa a la aplicación web alojada en **Vercel** (`https://soluciones-cloud-v1.vercel.app`). La interfaz está construida con **React y TypeScript** bajo el empaquetador **Vite**. Como es una Single Page Application (SPA), la carga es inmediata gracias a la red global CDN de Vercel. 

Apenas se dibuja la pantalla, el frontend realiza una petición pública de lectura (`GET /api/products`) hacia nuestro servidor backend en **Render** (`https://soluciones-cloud-v1.onrender.com`). El backend consulta la base de datos PostgreSQL en **Supabase** y devuelve la lista de productos disponibles. Hasta este punto, **cualquier usuario anónimo puede ver el catálogo**, lo que representa un acceso público y abierto."

---

### 2. El Proceso de Autenticación (Login con AWS Cognito y PKCE)
"Cuando el usuario desea realizar una acción protegida (como registrar un nuevo producto o eliminar uno existente), la aplicación le exige iniciar sesión. El usuario presiona el botón **'Iniciar sesión con Cognito'**.

Aquí entra en juego el protocolo **OAuth 2.0 con OpenID Connect (OIDC)** implementando **PKCE (Proof Key for Code Exchange)**. El frontend jamás le pide la contraseña al usuario en una ventanita local; en su lugar, la librería de React genera al vuelo una clave secreta aleatoria en memoria llamada `code_verifier` y le aplica una función criptográfica unidireccional SHA-256 para obtener un `code_challenge`. 

El navegador redirige al usuario a la interfaz gestionada de Amazon: la **Hosted UI de AWS Cognito**. El usuario introduce su correo y su contraseña **directamente en los servidores blindados de AWS**. Ni nuestro frontend en Vercel ni nuestro backend en Render ven jamás la contraseña del usuario, garantizando privacidad y cumplimiento de normativas internacionales de seguridad (GDPR/ISO 27001).

Una vez que Cognito comprueba que las credenciales son válidas, redirige al usuario de regreso a nuestra URL de Vercel con un **código de autorización temporal de un solo uso** en la barra de direcciones. El frontend atrapa ese código antes de que la página termine de renderizarse y hace una petición interna por detrás al endpoint `/oauth2/token` de Cognito, enviando el código recibido junto con la clave secreta original (`code_verifier`). 

Amazon calcula el hash del `code_verifier`, comprueba que coincide con el `code_challenge` que guardó al inicio y le entrega al frontend un paquete con tres llaves digitales en formato **JSON Web Token (JWT)**:
1. El **IdToken**: contiene los datos de perfil del usuario (su correo, su identificador único `sub`).
2. El **AccessToken**: contiene los permisos y scopes de autorización para invocar APIs.
3. El **RefreshToken**: permite renovar la sesión sin volver a pedir contraseña cuando los tokens expiran.

La pantalla de React detecta la sesión activa, muestra en la esquina superior el correo del usuario y habilita el formulario de creación de productos."

---

### 3. La Operación Protegida (Creación de Producto y Envío del JWT)
"Ahora el usuario autenticado completa el formulario: escribe el nombre del producto, la categoría, la descripción y el precio, y hace clic en **'Guardar Producto'**.

La aplicación en React toma el token JWT y construye una petición HTTP `POST` hacia la ruta `/api/products` de nuestro servidor en **Render**. En los encabezados (Headers) de la petición viaja el salvoconducto de seguridad:
`Authorization: Bearer <token_jwt>`."

---

### 4. La Validación Criptográfica en el Backend (Spring Security)
"La petición llega a través de Internet a nuestro contenedor Docker en **Render**. 

Antes de que la petición toque los controladores de la aplicación, el filtro perimetral de **Spring Security (OAuth2 Resource Server)** intercepta la llamada. Nuestro backend **no consulta una base de datos local ni le pregunta a un servidor central si el token es válido**. En su lugar, utiliza **criptografía asimétrica**:
1. **Descubrimiento de Claves:** Spring Security se conecta de forma segura a la dirección pública de claves de Amazon Cognito (`.well-known/jwks.json`) y descarga las claves públicas del User Pool (las cuales almacena en caché en memoria).
2. **Verificación de Firma (RS256):** El token fue firmado digitalmente por la clave privada de AWS. El backend usa la clave pública para verificar matemáticamente que nadie alteró el contenido del token.
3. **Validación de Claims:** Comprueba que la fecha de expiración (`exp`) sea válida y que el emisor (`iss`) corresponda exactamente a nuestro User Pool de AWS.

Si el token fue alterado, está vencido o no viene en la cabecera, Spring Security bloquea el paso de inmediato y devuelve un error **`HTTP 401 Unauthorized`** con el estándar RFC 6750."

---

### 5. La Persistencia en Base de Datos (Supabase PostgreSQL)
"Una vez que Spring Security certifica que el token es legítimo, le cede el control al método `createProduct()` de nuestro `ProductController`. 

El controlador delega en la capa de servicio y esta, a través de **Spring Data JPA y Hibernate**, traduce la entidad Java a una consulta SQL `INSERT INTO products...`.

La conexión viaja a través del **Session Pooler de Supabase en el puerto 5432**, que resuelve mediante IPv4 (evitando problemas de conectividad con Render) y es 100% compatible con el pool interno de conexiones **HikariCP** de Spring Boot. PostgreSQL guarda el registro permanentemente, le asigna su clave primaria autoincremental `id` y confirma la transacción."

---

### 6. Cierre del Ciclo (Respuesta Reactiva)
"Spring Boot devuelve una respuesta `HTTP 201 Created` con el producto creado en formato JSON. 

El frontend en React recibe la confirmación, inserta el nuevo producto en su estado local (`setProducts`) y la tarjeta del producto aparece de manera instantánea en la pantalla del usuario sin necesidad de recargar la página, completando con éxito un ciclo seguro, escalable y 100% en la nube."

---

# 📚 MÓDULO 2: CUESTIONARIO TÉCNICO DETALLADO (18 Preguntas Clave)

---

### 1. ¿Cuál fue el stack tecnológico usado en la solución?

* **Frontend:** React 18, TypeScript, Vite, `react-oidc-context`, `oidc-client-ts`, Vanilla CSS moderno con glassmorphism. Desplegado en **Vercel**.
* **Backend:** Java 21 LTS, Spring Boot 4.x / 3.x, Spring Security 6 (OAuth2 Resource Server), Spring Data JPA, Hibernate, Driver oficial de PostgreSQL (`org.postgresql:postgresql`), Docker multi-stage. Desplegado en **Render**.
* **Proveedor de Identidad (IDaaS):** AWS Cognito User Pools (Hosted UI, JWKS endpoint, flujos OAuth2 con PKCE).
* **Base de Datos:** PostgreSQL administrado en **Supabase** mediante Connection Pooler en modo sesión (puerto 5432).

---

### 2. Explicar la arquitectura del stack tecnológico

Responde a una **Arquitectura en 3 Capas Desacopladas Cloud-Native**:

```
[ CAPA 1: CLIENTE SPA ]          [ CAPA 2: IDaaS ]
   Vercel (React + Vite)  <---->   AWS Cognito
            | (HTTP + Bearer JWT)
            v
[ CAPA 3: SERVIDOR DE RECURSOS ]
   Render (Spring Boot + Docker)
            | (JDBC Session Pooler: 5432)
            v
[ CAPA 4: PERSISTENCIA ]
   Supabase (PostgreSQL)
```

* **Principio de Confianza Cero (Zero Trust):** Ningún componente confía a ciegas en el otro. El backend no asume que la petición es válida porque viene de Vercel; siempre exige y valida criptográficamente el JWT.
* **Sin Estado (*Stateless*):** Ni Render ni Vercel guardan sesiones en servidor. Cualquier instancia del backend puede atender a cualquier usuario en cualquier momento.

---

### 3. ¿Por qué se usa un IDaaS (Identity as a Service)?

1. **Eliminación de Riesgo Legal y de Seguridad:** No se almacenan contraseñas ni hashes en nuestras bases de datos. Si la base de datos de la empresa fuese vulnerada, los atacantes no obtendrán contraseñas porque están en AWS.
2. **Cumplimiento Normativo:** Cumple con GDPR, PCI-DSS, SOC 1/2/3 e ISO 27001.
3. **Capacidades Avanzadas Nativas:** Detección de ataques de fuerza bruta, políticas de contraseñas, soporte para MFA (Autenticación Multifactor) y recuperación de cuentas sin escribir una sola línea de código backend.
4. **Disponibilidad:** Alta disponibilidad global garantizada por el SLA de Amazon Web Services.

---

### 4. ¿Por qué se utiliza un API Gateway?

En arquitecturas empresariales, un **API Gateway** (como AWS API Gateway, Kong o Spring Cloud Gateway) actúa como puerta de enlace perimetral:
* **Offloading de Autenticación:** Valida el JWT en el borde de la red. Si el token es inválido, rechaza la petición sin consumir CPU ni memoria de los microservicios internos.
* **Enrutamiento Centralizado:** Oculta la topología de la red interna; los clientes solo conocen una única URL de entrada.
* **Rate Limiting y WAF:** Bloquea ataques DDoS y limita peticiones por IP o usuario.
* **CORS Unificado:** Resuelve las cabeceras de navegación en un solo punto.

---

### 5. Flujo de trabajo: Desde presionar Login hasta tener la sesión iniciada

1. El usuario presiona *"Iniciar sesión con Cognito"*.
2. El cliente genera `code_verifier` (secreto aleatorio) y `code_challenge = SHA256(code_verifier)`.
3. El navegador es redirigido a la **Hosted UI de Cognito** pasando el `code_challenge`, `client_id` y `redirect_uri`.
4. El usuario ingresa su usuario y contraseña en AWS.
5. AWS valida las credenciales y redirige a Vercel con un código de autorización temporal: `?code=XYZ`.
6. React intercepta el código y hace un POST interno a `/oauth2/token` enviando `code` y el `code_verifier`.
7. Cognito aplica SHA-256 al `code_verifier`, comprueba la coincidencia con el `code_challenge` inicial y entrega los tokens: `id_token`, `access_token` y `refresh_token`.
8. React guarda los tokens en almacenamiento seguro y actualiza el estado `auth.isAuthenticated = true`.

---

### 6. ¿Qué es el PKCE (Proof Key for Code Exchange)?

Es una extensión del estándar OAuth 2.0 (**RFC 7636**) diseñada para **clientes públicos** (Single Page Applications y móviles) que **no pueden almacenar un `client_secret` de forma segura**.

* **Problema:** En el navegador, el código fuente es accesible en la consola. Un secreto estático sería visible para cualquier atacante.
* **Solución de PKCE:** Crea un secreto dinámico único por cada login (`code_verifier`). Primero se envía el hash (`code_challenge`) para solicitar el código; luego se envía el secreto para canjearlo. Si un atacante intercepta el código de autorización en la URL, no podrá canjearlo porque no conoce el `code_verifier` original que solo existe en la memoria del navegador que inició el flujo.

---

### 7. Estructura de un JWT (JSON Web Token)

Un JWT consta de 3 secciones separadas por puntos (`.`):

$$\text{JWT} = \text{Header} . \text{Payload} . \text{Signature}$$

1. **Header (Encabezado):** Especifica el algoritmo (`RS256`), el tipo (`JWT`) y el identificador de la clave pública de Cognito (`kid`).
2. **Payload (Cuerpo / Claims):** Información de usuario y sesión codificada en Base64Url (legible, no cifrada). Contiene claims registrados como `sub`, `iss`, `exp` y personalizados como `email` y `cognito:groups`.
3. **Signature (Firma Criptográfica):** Es el hash del Header y Payload firmado digitalmente con la **clave privada de AWS Cognito** mediante RSA-SHA256:
   $$\text{Firma} = \text{RSA\_Sign}_{\text{ClavePrivada}}(\text{Base64}(H) + "." + \text{Base64}(P))$$

---

### 8. Si alguien modifica o hardcodea un rol en el JWT de "USER" a "ADMIN": ¿Dónde se modifica y qué ocurriría?

* **¿Dónde se modifica?** En el **Payload**. Al estar en Base64Url, cualquier persona puede decodificarlo, cambiar `"role": "USER"` por `"role": "ADMIN"`, re-codificarlo y enviar la nueva cadena.
* **¿Qué ocurriría exactamente?** **El token será rechazado automáticamente por el backend con un error HTTP 401 Unauthorized.**
  * *Explicación técnica:* Al cambiar un solo carácter en el Payload, su hash matemático cambia radicalmente.
  * Cuando Spring Security recibe la petición, toma la **clave pública** de Cognito y calcula la verificación sobre la firma adjunta.
  * Como el atacante no posee la **clave privada** de AWS para generar una nueva firma válida para ese payload alterado, la firma no cuadra con el contenido (`SignatureException` / `BadJwtException`). El token queda completamente invalidado.

---

### 9. Diferencia entre IdToken y AccessToken: ¿Cuál debe enviarse al backend?

| Atributo | IdToken (`id_token`) | AccessToken (`access_token`) |
| :--- | :--- | :--- |
| **Estándar** | OpenID Connect (OIDC). | OAuth 2.0 (RFC 6749/6750). |
| **Audiencia destino** | El **Cliente (Frontend)** para conocer los datos del usuario. | El **Servidor de Recursos (API/Backend)** para validar permisos. |
| **Contenido típico** | Datos personales: `name`, `email`, `sub`, `picture`. | Scopes y autorizaciones: `client_id`, `scope`, `token_use=access`. |
| **Uso en Backend** | Práctica común en Cognito si la API necesita claims de perfil directos sin llamar al endpoint `/userInfo`. | **El estándar estricto de OAuth2.** Es el portador formal de autorización. |

---

### 10. ¿Cómo se hicieron las implementaciones en el código?

1. **Fase Inicial (Manual con Nimbus JOSE):** Se implementó `JwtAuthenticationFilter` (extensión de `OncePerRequestFilter`) y `JwtValidationService`, los cuales descargaban manualmente las claves con `RemoteJWKSet`, parseaban el token y validaban fechas con condicionales `if`.
2. **Fase Definitiva (Nativa con Spring Security):** Se eliminó todo el código manual y se reemplazó por el starter oficial:
   * Dependencia: `spring-boot-starter-oauth2-resource-server`.
   * En `application.properties`:
     ```properties
     spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL
     ```
   * En `SecurityConfig.java`:
     ```java
     .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
     ```

---

### 11. Flujo lógico y cómo se valida el token en el backend

1. El filtro `BearerTokenAuthenticationFilter` de Spring intercepta la cabecera `Authorization: Bearer <token>`.
2. Lee la propiedad `issuer-uri` y consulta automáticamente el endpoint `/.well-known/openid-configuration` de Cognito para descubrir la URL de claves públicas (`jwks.json`).
3. Descarga y almacena en caché las claves públicas de AWS.
4. Identifica la clave correspondiente al `kid` (*Key ID*) del token y verifica matemáticamente la firma criptográfica (algoritmo asimétrico **RS256**).
5. Comprueba que el claim `iss` coincida de manera exacta con el User Pool de AWS y que la fecha de expiración `exp` sea posterior al tiempo actual del reloj del sistema.
6. Si la validación es exitosa, Spring Security crea un `JwtAuthenticationToken` y lo almacena en el `SecurityContextHolder`.

---

### 12. ¿Dónde está la configuración de CORS y qué hace?

Está en **[SecurityConfig.java](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/demo/src/main/java/com/example/demo/config/SecurityConfig.java)** a través del bean `CorsConfigurationSource`:
* Permite peticiones desde orígenes web cruzados (`allowedOriginPatterns("*")` o el dominio de Vercel).
* Habilita los métodos HTTP: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`.
* Autoriza la cabecera sensible `Authorization` (para que el navegador no bloquee el envío del Bearer token).
* Permite peticiones de sondeo previo **`OPTIONS` (Preflight)** sin exigir autenticación.

---

### 13. ¿Qué se configura en el IDaaS (Cognito) para conectar Frontend y Backend?

1. **App Client (Público):** Se deshabilita la generación de *Client Secret* (las aplicaciones React no pueden ocultar secretos).
2. **Allowed Callback URLs (Retorno):** Se declaran explícitamente:
   * `https://soluciones-cloud-v1.vercel.app/` *(Producción)*.
   * `http://localhost:5173/` *(Desarrollo local)*.
3. **Allowed Sign-out URLs (Cierre):** Las mismas URLs para permitir la redirección tras el logout.
4. **OAuth 2.0 Grant Types:** Se marca **Authorization Code Grant**.
5. **OpenID Connect Scopes:** Se activan `email`, `openid`, `phone`.
6. **Dominio de Cognito:** Se configura el prefijo para la Hosted UI (ej. `us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com`).

---

### 14. Explicar el flujo desde que se hace clic en "Agregar Producto"

1. El usuario completa los campos en `ProductForm.tsx` y presiona **"Guardar Producto"**.
2. `App.tsx` comprueba `auth.isAuthenticated`. Si no está autenticado, detiene la acción.
3. Invoca `productService.create(product, userToken)`.
4. El navegador envía una petición `POST /api/products` a Render con el encabezado `Authorization: Bearer <jwt>`.
5. Spring Security en Render valida la firma asimétrica del token contra las claves JWKS de Cognito.
6. `ProductController.java` recibe los datos y los envía a `ProductService.java`.
7. `ProductRepository.java` (Spring Data JPA) envía el comando `INSERT` a través del **Session Pooler de Supabase** (puerto 5432).
8. PostgreSQL en Supabase almacena el registro y le asigna un `id`.
9. Render responde `HTTP 201 Created` con el producto creado.
10. React actualiza su estado interno y la tarjeta del nuevo producto aparece reactivamente en la interfaz.

---

### 15. ¿Por qué se utilizó el Session Pooler (puerto 5432) en lugar de Direct Connection en Supabase?

1. **Incompatibilidad de IPv6 en Render:** La conexión directa de Supabase (`db.xxx.supabase.co`) solo tiene direcciones IPv6. Render (en su capa gratuita/estándar) no tiene enrutamiento IPv6 saliente, por lo que una conexión directa falla con `Connection timed out`. El Pooler de Supabase opera con direcciones **IPv4**.
2. **Compatibilidad con HikariCP y JPA:** Spring Boot maneja su propio pool de conexiones (**HikariCP**) y Hibernate utiliza sentencias preparadas (*Prepared Statements*). El **Session Pooler (puerto 5432)** mantiene la sesión JDBC viva durante la transacción, mientras que el *Transaction Pooler (puerto 6543)* rompe las conexiones inmediatamente al terminar el query, provocando colisiones con Hibernate a menos que se configure `prepareThreshold=0`.

---

### 16. Explicar la configuración de registro y confirmación de cuentas en Cognito

* **Envío automático de mensajes:** En *Autenticación -> Registro*, debe estar marcada la opción *"Permitir que Cognito envíe mensajes automáticamente para la verificación"*. De este modo, al registrarse, Cognito despacha inmediatamente el correo con el código de 6 dígitos.
* **Auto-confirmación:** En cuanto el usuario ingresa esos 6 dígitos en la Hosted UI, Cognito **cambia el estado del usuario a Confirmado automáticamente**, permitiéndole iniciar sesión sin intervención manual del administrador.
* **Lambda Pre Sign-up Trigger (Alternativa):** Si se asocia una función Lambda con `event.response.autoConfirmUser = true`, el paso del correo se salta y la cuenta queda aprobada en 0 segundos.

---

### 17. ¿Cómo se gestionan las variables de entorno en Render y Vercel?

* **En Vercel (Frontend):**
  * `redirect_uri: window.location.origin + '/'` se calcula dinámicamente en el navegador, adaptándose tanto a `localhost:5173` como a `https://soluciones-cloud-v1.vercel.app/`.
  * `API_BASE_URL` se conecta por defecto a `https://soluciones-cloud-v1.onrender.com/api/products` (o mediante `VITE_API_URL`).
* **En Render (Backend):**
  * `SPRING_DATASOURCE_URL`: `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres`
  * `SPRING_DATASOURCE_USERNAME`: `postgres.xuffliucihabnrodubse`
  * `SPRING_DATASOURCE_PASSWORD`: Credencial maestra de Supabase.
  * `SPRING_H2_CONSOLE_ENABLED`: `false`
  * `PORT`: Render inyecta dinámicamente el puerto del contenedor y Spring Boot lo adopta con `server.port=${PORT:8080}`.

---

### 18. Explicar todo (Síntesis Holística del Proyecto)

"Este proyecto es una muestra integral de ingeniería de software moderna en la nube:
* Un frontend desacoplado en **Vercel** que ofrece velocidad y experiencia de usuario.
* Un proveedor de identidad de estándar industrial en **AWS Cognito** que asume la responsabilidad crítica de las credenciales con **PKCE**.
* Un backend robusto y testeado en **Spring Boot / Java 21** dentro de un contenedor Docker en **Render**, que valida tokens mediante criptografía asimétrica **RS256** de forma nativa.
* Una base de datos relacional de alto rendimiento en **Supabase (PostgreSQL)** conectada a través de un pooler optimizado.

El resultado es una arquitectura segura, escalable, sin estado (*Stateless*), resiliente y completamente operativa en producción bajo costo cero."

---

# ⚠️ MÓDULO 3: PREGUNTAS TRAMPA DE EVALUADORES Y CÓMO RESPONDERLAS

| Pregunta del Evaluador | Respuesta Maestra |
| :--- | :--- |
| **"¿Dónde guardas las contraseñas de los usuarios en tu base de datos de Supabase?"** | *"En ninguna parte, profesor. Por diseño de seguridad, nuestro sistema jamás toca ni almacena contraseñas. Toda la gestión de credenciales, hashes y sales está delegada exclusivamente en AWS Cognito."* |
| **"Si yo intercepto el token JWT en tránsito y cambio mi rol a ADMIN, ¿puedo hackear tu API?"** | *"No, profesor. Aunque altere el rol en el Payload, la firma digital (Signature) fue calculada con la clave privada de AWS. Al cambiar cualquier dato, la firma se rompe. Spring Security validará contra la clave pública de Amazon, detectará el desajuste y rechazará la petición con HTTP 401 Unauthorized."* |
| **"¿Por qué tu backend no guarda sesiones en memoria ni usa cookies?"** | *"Porque implementamos una arquitectura Stateless. Al utilizar tokens JWT firmados, cada petición viaja con su propia prueba de autenticidad. Esto permite que el backend pueda escalar horizontalmente en múltiples instancias sin necesidad de compartir sesiones en memoria."* |
| **"¿Por qué usas PKCE si ya estás usando HTTPS?"** | *"HTTPS protege los datos contra espías en el cable de red (en tránsito), pero no protege el código de autorización contra aplicaciones o extensiones maliciosas instaladas dentro del propio dispositivo o navegador del cliente. PKCE garantiza que solo la instancia exacta de la aplicación que pidió el login sea la que pueda canjear el código por tokens."* |
| **"¿Qué pasa si Supabase se queda sin conexiones disponibles?"** | *"Para evitar la saturación de conexiones, conectamos nuestro backend mediante el Session Pooler en el puerto 5432. Esto desacopla las conexiones físicas de PostgreSQL de las conexiones lógicas que gestiona el pool interno HikariCP de Spring Boot, maximizando la concurrencia."* |

---

# 🌐 TABLA DE INFRAESTRUCTURA Y SERVICIOS EN VIVO

| Componente | Servicio / Proveedor | URL / Identificador |
| :--- | :--- | :--- |
| **Frontend SPA** | Vercel | `https://soluciones-cloud-v1.vercel.app` |
| **API Gateway** | Amazon API Gateway (REST) | `https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod` |
| **Backend API** | Render (Docker / Spring Boot) | `https://soluciones-cloud-v1.onrender.com` |
| **Identity Provider** | AWS Cognito User Pool | `us-east-1_OL9DjB9XL` (Client ID: `53acsgrc0tneq7jhigesj93g8r`) |
| **Database** | Supabase (PostgreSQL) | `aws-0-us-east-1.pooler.supabase.com:5432` |
| **Repositorio** | GitHub | `https://github.com/ignSf/soluciones_cloud_v1` |


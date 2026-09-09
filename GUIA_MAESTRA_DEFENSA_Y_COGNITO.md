# Guía Maestra de Estudio: Implementación de AWS Cognito, Flujo Narrativo y Defensa Técnica

> **Proyecto:** Soluciones Cloud v1  
> **Servicios:** Vercel (Frontend) · Amazon API Gateway · Render (Backend) · AWS Cognito (IDaaS) · Supabase (PostgreSQL)  
> **Objetivo:** Material de estudio completo y directo para dominar la implementación de Cognito desde cero, explicar el flujo con palabras sencillas y responder con solidez todas las preguntas del examen o presentación.

---

# 🎙️ PARTE 1: EL FLUJO EN PALABRAS SIMPLES (Qué decir en tu presentación)

> [!TIP]
> **Estrategia para la defensa:** Habla con calma, usando analogías claras. Lo que más valoran los evaluadores no es memorizar código, sino **entender por qué cada pieza está ahí y cómo viaja la seguridad**.

### 1. La bienvenida (Carga inicial)
*"Nuestra aplicación web está alojada en **Vercel** (`https://soluciones-cloud-v1.vercel.app`). Al entrar, cualquier persona puede ver el catálogo de productos de inmediato. Esto es **acceso público**: el frontend consulta a nuestro backend a través de **Amazon API Gateway** y muestra los productos guardados en **Supabase**. No se le exige iniciar sesión para mirar."*

### 2. El Login (Delegación en AWS Cognito y PKCE)
* **Lo que debes resaltar:** *"Nosotros **no manejamos contraseñas**. No tenemos tablas de usuarios con contraseñas en Supabase ni en Render."*
* **La explicación:** *"Cuando el usuario quiere agregar o eliminar un producto, hace clic en **'Iniciar sesión con Cognito'**. En ese instante, nuestra aplicación en React no abre un formulario propio, sino que redirige al usuario a una pantalla segura administrada por Amazon Web Services: la **Hosted UI de AWS Cognito**.*
* **El toque técnico clave:** *Para evitar que alguien intercepte la comunicación, usamos **PKCE** (Proof Key for Code Exchange). El navegador genera un secreto al vuelo (`code_verifier`) y envía su huella digital (`code_challenge`) a Amazon. El usuario escribe sus credenciales directamente en los servidores de AWS. Amazon valida la cuenta y nos devuelve un código temporal a Vercel. El frontend canjea ese código junto con su secreto original y Amazon le entrega **3 tokens JWT**: el IdToken (con los datos de la persona), el AccessToken (con los permisos) y el RefreshToken (para mantener la sesión)."*

### 3. La creación de producto (El token como pasaporte)
* **Lo que debes resaltar:** *"El token JWT funciona exactamente como un **pasaporte digital**."*
* **La explicación:** *"El usuario escribe el nombre, precio y categoría de un producto y presiona 'Guardar'. El frontend empaqueta los datos y hace una petición `POST` al **Amazon API Gateway**, adjuntando el token en la cabecera: `Authorization: Bearer <token>`."*

### 4. El doble filtro de seguridad (API Gateway + Spring Security)
* **Lo que debes resaltar:** *"Tenemos una **defensa en profundidad** con dos capas de seguridad:"*
  1. **Primera línea de defensa (Amazon API Gateway):** *"API Gateway tiene un **Cognito Authorizer**. Antes de molestar a nuestro servidor, Amazon revisa el token en el perímetro. Si alguien manda un token falso o no manda nada, API Gateway lo frena de golpe con un `HTTP 401 Unauthorized` sin consumir recursos de nuestro backend."*
  2. **Segunda línea de defensa (Spring Security en Render):** *"Si el token es legítimo, API Gateway deja pasar la petición hacia nuestro servidor Spring Boot en **Render**. Allí, Spring Security verifica de forma nativa y asimétrica (RS256) que el token fue firmado por la clave privada de AWS descargando las claves públicas (JWKS)."*

### 5. El guardado en la base de datos (Supabase PostgreSQL)
* **Lo que debes resaltar:** *"Usamos una base de datos relacional en la nube conectada por un Session Pooler."*
* **La explicación:** *"Spring Boot toma los datos del producto y mediante Hibernate/JPA ejecuta el `INSERT` hacia **Supabase** a través del puerto `5432` (Session Pooler). Supabase guarda el producto con un ID autoincremental permanente, Render responde con `HTTP 201 Created` y la pantalla en Vercel se actualiza en tiempo real sin recargar la página."*

---

# 🛠️ PARTE 2: PASO A PASO DE CÓMO IMPLEMENTAR AWS COGNITO DESDE CERO

*Si en la evaluación te preguntan: **"¿Cómo creaste y configuraste Cognito para que funcione con tu frontend y backend?"**, este es el procedimiento exacto:*

```
[ 1. Crear User Pool ] ➔ [ 2. Configurar Registro ] ➔ [ 3. Crear App Client Público ]
                                                                 │
[ 6. Conectar en React ] ◄─── [ 5. Definir URLs ] ◄─── [ 4. Crear Dominio Hosted UI ]
```

### Paso 1: Crear el User Pool (Grupo de Usuarios)
1. Entrar a la consola de **AWS Cognito** y pulsar **"Crear grupo de usuarios"** (Create user pool).
2. **Opciones de inicio de sesión:** Seleccionar **Correo electrónico** (Email).
3. **Requisitos de contraseña:** Dejar los estándares (mínimo 8 caracteres, mayúsculas, minúsculas, números y caracteres especiales).
4. **MFA (Autenticación Multifactor):** Seleccionar *"Sin MFA"* (No MFA) para agilizar pruebas de desarrollo, o dejarlo opcional.

### Paso 2: Configurar la Experiencia de Registro y Mensajes Automáticos
1. En **Auto-registro:** Asegurarse de que esté activada la opción *"Permitir que los usuarios se registren ellos mismos"*.
2. En **Verificación de atributos:**
   * ✅ Marcar: **"Permitir que Cognito envíe mensajes automáticamente para la verificación y confirmación"**.
   * 🔘 Seleccionar: **"Enviar mensaje de correo electrónico, verificar la dirección de correo electrónico"**.
   *(Esto es lo que permite que al registrarse le llegue el código de 6 dígitos al usuario y al ingresarlo se auto-apruebe la cuenta sin intervención manual).*

### Paso 3: Crear el Cliente de Aplicación (App Client) para la SPA
1. **Tipo de cliente:** Elegir **"Aplicación de página única (SPA)"** o Cliente Público.
2. **Nombre del cliente:** ej. `cloud-cognito`.
3. ⚠️ **Secreto de cliente (Client Secret):** Seleccionar **"No generar un secreto de cliente"** *(CRUCIAL: las aplicaciones React en el navegador son públicas y no pueden esconder un secret)*.
4. **Flujos de autenticación:** Asegurar que esté marcado **SRP (Secure Remote Password)** y flujos de usuario.

### Paso 4: Crear el Dominio para la Hosted UI
1. Ir a la pestaña **Creación de marca** -> **Dominio** (o en la configuración del cliente).
2. Seleccionar **"Usar un prefijo de dominio de Cognito"**.
3. Escribir un nombre único (ej. `us-east-1ol9djb9xl`).
4. La URL resultante será la Hosted UI: `https://<dominio>.auth.us-east-1.amazoncognito.com`.

### Paso 5: Configurar las URLs de Retorno (Callback & Sign-out URLs)
1. Dentro del App Client, ir a la pestaña **"Páginas de inicio de sesión"** (Login pages) -> **Editar**:
   * **URL de devolución de llamadas permitidas (Allowed callback URLs):**
     * `https://soluciones-cloud-v1.vercel.app/` *(para producción)*.
     * `http://localhost:5173/` *(para desarrollo local)*.
   * **URL de cierre de sesión permitidas (Allowed sign-out URLs):**
     * `https://soluciones-cloud-v1.vercel.app/`
     * `http://localhost:5173/`
2. **Tipos de concesión de OAuth 2.0:** Seleccionar **Concesión de código de autorización** (Authorization code grant) con PKCE.
3. **Ámbitos (Scopes):** Marcar `openid`, `email`, `phone`.
4. Guardar los cambios.

### Paso 6: Conectar en el Frontend (React + Vite)
En el archivo `authConfig.ts`:
```typescript
export const cognitoAuthConfig: AuthProviderProps = {
  authority: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',
  client_id: '53acsgrc0tneq7jhigesj93g8r',
  redirect_uri: window.location.origin + '/', // Dinámico para local y Vercel
  response_type: 'code',
  scope: 'email openid phone',
};
```

### Paso 7: Conectar en el Backend (Spring Boot)
En `application.properties`:
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL
```
*(Spring Boot se encarga de descargar automáticamente las claves públicas de AWS y validar la firma de cada token).*

---

# 🧠 PARTE 3: PREGUNTAS TÉCNICAS EXPLICADAS Y RESPONDIDAS

---

### 1. ¿Cuál fue el stack tecnológico usado en la solución?
* **Frontend:** React 18, TypeScript, Vite, `react-oidc-context`, `oidc-client-ts`, Vanilla CSS (Vercel).
* **Pasarela de Entrada:** Amazon API Gateway con Cognito Authorizer y proxy HTTP.
* **Backend:** Java 21, Spring Boot, Spring Security 6 (OAuth2 Resource Server), Spring Data JPA, Hibernate, Docker (Render).
* **Identidad (IDaaS):** AWS Cognito User Pool (Hosted UI con PKCE).
* **Persistencia:** PostgreSQL en Supabase mediante Session Pooler (puerto 5432).

---

### 2. Explicar la arquitectura del stack tecnológico
Es una arquitectura desacoplada de 3 niveles:
* **Capa Cliente (Vercel):** Se encarga únicamente de la experiencia de usuario y del flujo de autenticación mediante OIDC/PKCE.
* **Capa Perimetral y de Lógica (API Gateway + Render):** API Gateway filtra y autoriza en el borde; Spring Boot ejecuta la lógica de negocio y aplica validación criptográfica de tokens sin estado (*Stateless*).
* **Capa de Persistencia (Supabase):** Base de datos relacional PostgreSQL conectada por un pool de conexiones optimizado para la nube.

---

### 3. ¿Por qué se usa IDaaS (Identity as a Service)?
1. **Seguridad y Confianza:** No almacenamos credenciales de usuarios. Toda la responsabilidad de contraseñas, cifrado y fuerza bruta recae en AWS.
2. **Cumplimiento Legal:** Cumple con GDPR, SOC y PCI-DSS sin costo de desarrollo.
3. **Escalabilidad:** Soporta millones de usuarios concurrentes de forma transparente.
4. **MFA y Recuperación:** Funciones de seguridad complejas listas para usar.

---

### 4. ¿Por qué se utiliza API Gateway?
1. **Punto Único de Entrada:** Oculta la dirección real de los servidores internos de backend.
2. **Offloading de Autenticación:** Valida el token JWT en el borde de Amazon; si el token es falso o expiró, rechaza la llamada antes de que llegue a Render, ahorrando CPU, memoria y costos.
3. **Rate Limiting:** Protege contra ataques DoS y saturación de peticiones.
4. **CORS Centralizado:** Gestiona los permisos entre orígenes en una sola capa.

---

### 5. Flujo de trabajo: Desde presionar Login hasta tener la sesión iniciada
1. Clic en *"Iniciar sesión con Cognito"*.
2. Frontend genera el secreto `code_verifier` y calcula su hash `code_challenge`.
3. Redirección hacia la Hosted UI de Cognito enviando el `code_challenge`.
4. El usuario escribe su usuario y contraseña en AWS.
5. AWS valida las credenciales y redirige a Vercel con un código de autorización temporal (`?code=...`).
6. El frontend intercepta el código y hace un POST interno a `/oauth2/token` enviando el `code` y el `code_verifier`.
7. Cognito comprueba que el hash del `code_verifier` coincida con el `code_challenge` inicial.
8. Cognito entrega el IdToken, AccessToken y RefreshToken.
9. React guarda la sesión y activa el estado autenticado.

---

### 6. ¿Qué es el PKCE y por qué es obligatorio en SPAs?
* **PKCE (Proof Key for Code Exchange, RFC 7636)** es una extensión de seguridad para clientes públicos (React, Angular, apps móviles).
* En el navegador, **el código fuente es público**: no se puede guardar un `client_secret` privado porque cualquiera lo vería en la consola.
* PKCE sustituye el secreto estático por un secreto dinámico generado al vuelo (`code_verifier`). Al enviar primero el hash (`code_challenge`), si un hacker intercepta el código de autorización en la URL, **no podrá canjearlo** porque no conoce el secreto original que solo vive en la memoria de ese navegador.

---

### 7. Estructura de un JWT (JSON Web Token)
Se compone de 3 partes separadas por puntos (`.`):
$$\text{Header} . \text{Payload} . \text{Signature}$$
1. **Header (Encabezado):** Algoritmo (`RS256`), tipo (`JWT`) y el ID de la clave de Amazon (`kid`).
2. **Payload (Datos / Claims):** Información del usuario codificada en Base64Url (legible, no cifrada): `sub`, `email`, `iss`, `exp`.
3. **Signature (Firma):** El hash matemático del Header y Payload firmado digitalmente con la **clave privada de AWS Cognito**.

---

### 8. ¿Qué pasa si alguien altera un rol hardcodeándolo de USER a ADMIN en el JWT?
* **¿Dónde se cambia?** En el **Payload** (decodificando el Base64Url y volviéndolo a codificar).
* **¿Qué ocurriría exactamente?** **El backend lo rechaza inmediatamente con un error HTTP 401 Unauthorized.**
  * Al cambiar una sola letra del Payload, el hash matemático cambia por completo.
  * Cuando Spring Security (o API Gateway) verifica la firma usando la **clave pública** de Amazon, el cálculo matemático no cuadra con la firma adjunta.
  * Como el atacante no tiene la **clave privada** de Amazon para firmar el nuevo contenido alterado, es criptográficamente imposible falsificarlo.

---

### 9. Diferencia entre IdToken y AccessToken: ¿Cuál debe enviarse al backend?
* **IdToken (OpenID Connect):** Diseñado para el **Frontend**. Le informa a la interfaz *quién* es el usuario (`email`, `sub`, nombre).
* **AccessToken (OAuth 2.0):** Diseñado para el **Servidor de Recursos (Backend)**. Porta las autorizaciones, alcances (`scopes`) y permisos.
* **Criterio formal:** Para autorizar peticiones a APIs debe enviarse el **AccessToken**. (Cognito también permite validar el IdToken en backend si se necesitan datos de perfil directos sin llamar al endpoint `/userInfo`).

---

### 10. ¿Cómo se hicieron las implementaciones en el código?
* **Antes (Manual):** Se programaron las clases `JwtAuthenticationFilter` y `JwtValidationService` usando la librería Nimbus JOSE, descargando claves públicas a mano con `RemoteJWKSet` y comparando fechas con condicionales `if`.
* **Ahora (Nativo Spring Security):** Se migró a `spring-boot-starter-oauth2-resource-server`. Con solo declarar `spring.security.oauth2.resourceserver.jwt.issuer-uri` en `application.properties` y `.oauth2ResourceServer()` en `SecurityConfig.java`, Spring Security gestiona el ciclo de vida, descarga de JWKS, validación de emisor y expiración de forma 100% nativa.

---

### 11. ¿Dónde está la configuración de CORS y por qué es necesaria?
* Está en **[SecurityConfig.java](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/demo/src/main/java/com/example/demo/config/SecurityConfig.java)** y en las opciones de **Amazon API Gateway**.
* Es obligatoria porque los navegadores bloquean por seguridad peticiones JavaScript entre distintos dominios (ej. de Vercel a API Gateway o Render).
* Habilita métodos `GET, POST, OPTIONS` y permite explícitamente el encabezado `Authorization`.

---

### 12. ¿Qué se configura en el IDaaS para conectar Frontend y Backend?
1. **User Pool:** Atributos obligatorios y verificación de correo activada.
2. **App Client Público:** Sin client secret, con flujo Authorization Code Grant + PKCE.
3. **Hosted UI Domain:** Subdominio de Amazon para mostrar la pantalla de login.
4. **Allowed Callback y Sign-out URLs:** Las URLs autorizadas de Vercel y localhost.
5. **Scopes:** `openid`, `email`, `phone`.

---

### 13. ¿Por qué usar Session Pooler (puerto 5432) y no Direct Connection en Supabase?
1. **El bloqueo de IPv6 en Render:** La conexión directa de Supabase funciona solo por IPv6, pero los servidores de Render no tienen enrutamiento saliente IPv6. El Pooler de Supabase opera con **IPv4**.
2. **Compatibilidad con HikariCP / Hibernate:** El modo Session (puerto 5432) mantiene viva la sesión mientras dura la transacción de Spring Boot, permitiendo el uso normal de sentencias preparadas (*Prepared Statements*) sin generar colisiones.

---

# 🏆 TABLA RESUMEN PARA MEMORIZAR ANTES DE ENTRAR A DEFENDER

| Pregunta Típica | Respuesta Corta y Contundente |
| :--- | :--- |
| **¿Dónde están las contraseñas en tu base de datos?** | *"En ninguna parte. La gestión de contraseñas está 100% delegada en AWS Cognito."* |
| **¿El backend guarda sesiones?** | *"No, es 100% Stateless. Cada petición se valida con la firma digital del JWT."* |
| **¿Por qué API Gateway si ya tienes backend?** | *"Para offloading de seguridad: bloquea tokens inválidos en el perímetro sin gastar recursos de Render."* |
| **¿Por qué PKCE?** | *"Porque en una SPA el código es público y no podemos guardar contraseñas secretas."* |
| **¿Qué pasa si modifico el JWT en el navegador?** | *"Se rompe la firma digital (Signature) y el backend responde HTTP 401 Unauthorized."* |

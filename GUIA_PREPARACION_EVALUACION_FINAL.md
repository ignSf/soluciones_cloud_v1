# Guía Integral de Preparación y Defensa: Evaluación Final Cloud (30 Minutos)

> **Documento Oficial de Preparación de Examen**  
> **Ecosistema en Producción:** [Vercel](https://soluciones-cloud-v1.vercel.app) (Frontend) · [Amazon API Gateway](https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod) · [Render](https://soluciones-cloud-v1.onrender.com) (Backend Docker) · [AWS Cognito](https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com) (IDaaS) · [Supabase](https://supabase.com) (PostgreSQL)  
> **Criterio de Formato:** Explicación clara sin enredos técnicos innecesarios, enfocada en la rúbrica oficial y con los conceptos más importantes resaltados en <mark>amarillo</mark>.

---

## 🎯 PARTE 1: CUMPLIMIENTO EXACTO DE LA RÚBRICA DEL PROFESOR

*Esta sección demuestra cómo nuestro proyecto cumple al 100% con cada una de las exigencias del enunciado:*

| Exigencia del Profesor | Estado en Nuestro Proyecto | ¿Cómo demostrarlo en la presentación? |
| :--- | :---: | :--- |
| <mark>**Presentación máxima de 30 minutos**</mark> | ✅ Cumplido | Seguir el cronograma de 4 bloques (Demo ➔ Arquitectura ➔ Seguridad ➔ Preguntas). |
| <mark>**Solución completamente funcional y operativa**</mark> | ✅ Cumplido | El sistema está desplegado en vivo en internet. No corre en `localhost`. |
| <mark>**Backend compila, buenas prácticas y pruebas**</mark> | ✅ Cumplido | Java 21 + Spring Boot compila con Maven (`BUILD SUCCESS`) y pasa los tests unitarios (`DemoApplicationTests`). |
| <mark>**Frontend completo, modular y sin errores**</mark> | ✅ Cumplido | React 18 + TypeScript modular (`Navbar`, `ProductForm`, `ProductList`, `ProductCard`, `services`, `auth`). |
| <mark>**Backend incluye filtros que validan JWT del IDaaS**</mark> | ✅ Cumplido | Implementado con `spring-boot-starter-oauth2-resource-server`. Valida firma RS256, expiración y emisor de AWS. |
| <mark>**Frontend implementa login con IDaaS y envía JWT**</mark> | ✅ Cumplido | `react-oidc-context` maneja el login con Cognito vía **PKCE** y adjunta `Authorization: Bearer <token>` en cada `fetch`. |
| <mark>**Uso correcto de `.gitignore` (solo código necesario)**</mark> | ✅ Cumplido | `.gitignore` configurado en raíz, backend y frontend. Cero binarios, sin `target/`, sin `node_modules/` ni credenciales. |
| <mark>**Todos los integrantes preparados para explicar**</mark> | ✅ Cumplido | Todo el equipo debe dominar las preguntas y analogías de esta guía. |

---

## ⏱️ PARTE 2: GUION CRONOMETRADO PARA LOS 30 MINUTOS

Para no perder tiempo y mostrar seguridad frente a la comisión, dividan la presentación en estos 4 bloques:

```
[ Min 0 - 5: Introducción ] ➔ [ Min 5 - 15: Demo en Vivo ] ➔ [ Min 15 - 24: Arquitectura y Código ] ➔ [ Min 24 - 30: Preguntas ]
```

### Bloque 1: Introducción y Planteamiento (Minutos 0 a 5)
* Presentar a los integrantes del equipo.
* Explicar el objetivo: <mark>Una solución de comercio/inventario desacoplada, 100% en la nube y sin servidores locales que mantener</mark>.
* Mencionar los 5 servicios en vivo: Vercel, Amazon API Gateway, Render, AWS Cognito y Supabase.

### Bloque 2: Demostración Práctica en Vivo (Minutos 5 a 15) *(¡La parte que más puntos da!)*
1. **Mostrar el catálogo público:** Abrir [https://soluciones-cloud-v1.vercel.app](https://soluciones-cloud-v1.vercel.app). Explicar que cualquier persona puede leer productos sin cuenta.
2. **Demostrar el bloqueo sin login:** Intentar crear o borrar un producto sin haber iniciado sesión. Mostrar que la aplicación y el backend bloquean la acción.
3. **Flujo de Login:** Presionar *"Iniciar sesión con Cognito"*. Mostrar la pantalla segura de AWS (Hosted UI) e ingresar las credenciales.
4. **Creación de Producto:** Con la sesión iniciada, llenar el formulario (ej: *"Teclado Mecánico RGB"*, precio `$49.99`) y presionar *"Guardar Producto"*.
5. **Verificación en Supabase:** Abrir la consola de Supabase en vivo y mostrar la fila recién insertada en la tabla `products`.

### Bloque 3: Explicación de Código, Seguridad y Repositorio (Minutos 15 a 24)
* Mostrar brevemente el repositorio en GitHub y destacar los archivos <mark>`.gitignore` limpios</mark> (sin basura ni dependencias pesadas).
* Explicar cómo el frontend usa **PKCE** y cómo el backend valida el **JWT** de forma nativa con Spring Security.
* Mostrar la regla perimetral en **Amazon API Gateway**.

### Bloque 4: Cierre y Preguntas del Docente (Minutos 24 a 30)
* Ceder la palabra al profesor y responder con las respuestas maestras de esta guía.

---

## 💡 PARTE 3: EL FLUJO EXPLICADO EN PALABRAS SIMPLES (Sin tecnicismos innecesarios)

*Para entenderlo y explicarlo sin memorizar palabras difíciles, usemos una analogía cotidiana:*

> [!NOTE]
> ### La Analogía del Festival de Música y la Pulsera VIP
> * **El Usuario:** Es una persona que quiere entrar a la zona VIP de un festival de música (crear productos).
> * **AWS Cognito (IDaaS):** Es la **boletería oficial** en la entrada del evento. La persona muestra su carnet de identidad, compra su entrada y la boletería le coloca una **pulsera sellada con un holograma oficial infalsificable (el JWT)**.
> * **Amazon API Gateway:** Es el **guardia de seguridad en la reja perimetral**. Mira que la persona tenga la pulsera puesta. Si no tiene pulsera, no la deja ni pisar el pasto (`401 Unauthorized`).
> * **Spring Boot en Render (Backend):** Es el **coordinador dentro de la carpa VIP**. Con una luz ultravioleta (las claves públicas de Amazon) verifica que el holograma de la pulsera sea auténtico y no esté vencido.
> * **Supabase (Base de Datos):** Es la **bodega o inventario**. Cuando el coordinador autoriza al usuario, se guarda el producto en los estantes.

---

### El Flujo Real en 5 Pasos Claros:

#### 1. Navegación Libre (Pública)
El usuario entra a Vercel. La pantalla le pide los productos a Render pasando por API Gateway. <mark>Ver el catálogo no requiere clave ni permisos</mark>.

#### 2. El Inicio de Sesión
El usuario toca *"Iniciar sesión con Cognito"*. En lugar de pedirle la contraseña en nuestra web, lo mandamos a los servidores de Amazon.
* <mark>**¿Por qué hacemos esto?** Por seguridad. Si nuestra base de datos sufriera un ataque, el atacante no encontrará ninguna contraseña porque nosotros jamás las tocamos ni las guardamos; están bajo el cuidado de Amazon.</mark>

#### 3. El Desafío Criptográfico (PKCE)
Como nuestra web corre en el navegador de cualquier persona, cualquier usuario curioso podría abrir la consola y ver el código.
* Para evitar que un hacker intercepte el código de inicio de sesión en el camino, usamos **PKCE**: <mark>el navegador inventa una clave secreta al vuelo y solo se la muestra a Amazon al final del canje</mark>. Así, nadie puede robarse la sesión.

#### 4. La Entrega del Token JWT (El Pasaporte)
Amazon confirma que el usuario existe y le entrega al navegador un archivo de texto codificado llamado **JWT**. Este token contiene la identidad del usuario y viene **sellado digitalmente con una firma matemática de AWS**.

#### 5. Guardar el Producto con el Token en Mano
Cuando el usuario presiona *"Guardar Producto"*, el navegador mete el token en el encabezado de la petición: `Authorization: Bearer <token>`.
1. **API Gateway** revisa que el token venga presente.
2. **Spring Security en Render** usa las claves públicas de Amazon para certificar que la firma matemática es real y que no ha expirado.
3. Se ejecuta el guardado en **Supabase** a través de una conexión optimizada (Session Pooler).
4. El producto aparece de inmediato en la pantalla.

---

# 📖 PARTE 4: BANCO DE PREGUNTAS Y RESPUESTAS PARA LA DEFENSA

---

### 1. ¿Cuál es el stack tecnológico usado en la solución?
* **Frontend:** React 18, TypeScript, Vite y librería de autenticación OIDC. Alojado en **Vercel**.
* **Pasarela Perimetral:** **Amazon API Gateway** (REST API con proxy HTTP).
* **Backend:** Java 21, Spring Boot, Spring Security 6 (OAuth2 Resource Server) y Docker. Alojado en **Render**.
* **Proveedor de Identidad:** **AWS Cognito** (User Pool con Hosted UI).
* **Base de Datos:** PostgreSQL en la nube mediante **Supabase** (Session Pooler en puerto 5432).

---

### 2. ¿Cómo es la arquitectura del stack tecnológico?
Es una <mark>**arquitectura en 3 niveles desacoplada y sin estado (Stateless)**</mark>:
* Cada componente tiene una única responsabilidad: el frontend muestra la interfaz; Cognito gestiona identidades; API Gateway filtra en el borde; Spring Boot procesa la lógica; Supabase almacena los datos.
* **Sin estado (*Stateless*):** <mark>El servidor backend no guarda sesiones en memoria ni cookies</mark>. Cada petición viaja con su propio token firmado. Si tenemos 1 servidor o 100 servidores, cualquiera puede atender la petición.

---

### 3. ¿Por qué se usa un IDaaS (Identity as a Service)?
1. <mark>**Cero riesgo de filtración de contraseñas:**</mark> No guardamos contraseñas en nuestra base de datos.
2. **Cumplimiento legal y normativo:** Cumple con GDPR, ISO 27001 y SOC sin costo de desarrollo.
3. **Funciones automáticas:** Detección de ataques de fuerza bruta, políticas de contraseñas complejas y recuperación de cuentas listas para usar.

---

### 4. ¿Por qué se utiliza un API Gateway?
1. **Punto único de acceso:** Oculta la dirección real de nuestros servidores internos.
2. <mark>**Descarga de seguridad (Offloading):**</mark> Bloquea peticiones no autenticadas en el perímetro de Amazon antes de que lleguen a Render, ahorrando memoria y procesador en el backend.
3. **Control y Protección:** Permite limitar la cantidad de peticiones por segundo (Rate Limiting) y centralizar el control de CORS.

---

### 5. Explicar el flujo desde presionar Login hasta tener la sesión iniciada
1. El usuario hace clic en *"Iniciar sesión con Cognito"*.
2. El frontend genera el secreto `code_verifier` y calcula su huella `code_challenge` (**PKCE**).
3. Redirige a la pantalla oficial de AWS Cognito enviando la huella.
4. El usuario escribe sus credenciales en la web de Amazon.
5. Cognito valida los datos y devuelve al navegador un código temporal en la URL (`?code=...`).
6. El frontend atrapa ese código y lo canjea junto con su secreto original (`code_verifier`) ante Amazon.
7. Cognito comprueba que coincidan y <mark>entrega los 3 tokens: IdToken, AccessToken y RefreshToken</mark>.
8. React guarda la sesión y activa la vista de usuario autenticado.

---

### 6. ¿Qué es el PKCE y por qué es obligatorio en aplicaciones web SPA?
* **PKCE (Proof Key for Code Exchange):** Es un mecanismo de seguridad para aplicaciones que corren en el navegador o en celulares (clientes públicos).
* <mark>**El motivo:** En una página web en React el código es público (cualquiera puede inspeccionarlo), por lo que **no se puede guardar una contraseña secreta del sistema (*client_secret*)**</mark>.
* **Cómo lo resuelve:** Inventa un secreto dinámico temporal por cada inicio de sesión, impidiendo que un atacante que espíe la URL pueda canjear el código por tokens.

---

### 7. ¿Cuál es la estructura de un JWT (JSON Web Token)?
Es una cadena de texto dividida en 3 partes separadas por puntos (`.`):
$$\text{Header} . \text{Payload} . \text{Signature}$$
1. **Header (Encabezado):** Dice qué algoritmo se usó (`RS256`) y qué clave de Amazon lo firmó (`kid`).
2. **Payload (Datos):** <mark>Contiene la información del usuario en texto legible Base64Url (NO está cifrado)</mark>: correo, ID de usuario (`sub`) y fecha de expiración (`exp`).
3. **Signature (Firma):** Es el sello criptográfico calculado con la **clave privada de AWS**. Garantiza que nadie haya cambiado una sola letra de los datos.

---

### 8. Si alguien modifica un rol hardcodeándolo de USER a ADMIN en el JWT: ¿Dónde se cambia y qué ocurriría?
* **¿Dónde se cambia?** En el **Payload**, decodificando el texto en Base64Url y volviéndolo a codificar con el rol cambiado.
* **¿Qué ocurriría exactamente?** <mark>**El sistema lo rechaza de inmediato con un error HTTP 401 Unauthorized**</mark>.
  * *Razón:* Al alterar el contenido del Payload, el cálculo matemático de la firma ya no coincide.
  * El backend (y API Gateway) comprueba la firma usando la **clave pública** de Amazon.
  * Como el atacante no tiene la **clave privada** de AWS para volver a firmar el token adulterado, el sistema detecta la trampa al instante y bloquea la petición.

---

### 9. ¿Cuál es la diferencia entre IdToken y AccessToken? ¿Cuál debe enviarse al backend?
* **IdToken (Identidad):** Hecho para el **Frontend**. Contiene los datos personales del usuario (nombre, correo) para mostrarlos en la barra superior.
* **AccessToken (Autorización):** Hecho para el **Backend / API**. Contiene los permisos y privilegios para autorizar operaciones.
* <mark>**Criterio de envío:** El estándar formal de OAuth 2.0 exige enviar el **AccessToken**</mark>. (En proyectos con Cognito, muchas APIs también aceptan el IdToken si el backend necesita extraer directamente el correo del usuario sin consultar endpoints adicionales).

---

### 10. ¿Cómo se hicieron las implementaciones en el código?
* **Antes (Enfoque manual):** Teníamos dos clases personalizadas (`JwtAuthenticationFilter` y `JwtValidationService`) que usaban la biblioteca Nimbus para descargar claves y validar fechas con código manual.
* **Ahora (Enfoque nativo Spring Security):** <mark>Migramos al estándar oficial `spring-boot-starter-oauth2-resource-server`</mark>. Solo declaramos el emisor de Cognito en `application.properties` y Spring Security se encarga automáticamente de descargar las claves públicas, verificar firmas y proteger los endpoints.

---

### 11. ¿Dónde está la configuración de CORS y por qué es necesaria?
* Está configurada en **[SecurityConfig.java](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/demo/src/main/java/com/example/demo/config/SecurityConfig.java)** y en **Amazon API Gateway**.
* <mark>**Por qué es necesaria:**</mark> Los navegadores impiden por seguridad que una web en un dominio (`vercel.app`) haga peticiones a otro dominio diferente (`amazonaws.com` o `onrender.com`), a menos que el servidor declare explícitamente que autoriza esa comunicación y permita enviar la cabecera `Authorization`.

---

### 12. ¿Qué se configura en el IDaaS (Cognito) para conectar Frontend y Backend?
1. **App Client Público:** Configurado sin *Client Secret* para permitir autenticación segura desde React.
2. <mark>**Allowed Callback URLs:**</mark> Registro de `https://soluciones-cloud-v1.vercel.app/` y `http://localhost:5173/` para que Amazon autorice devolver los códigos.
3. **Flujo de concesión:** Marcado *Authorization code grant* con soporte PKCE.
4. **Dominio Hosted UI:** Creación del subdominio donde reside la pantalla de login de Amazon.

---

### 13. Explicar el flujo desde que se hace clic en "Agregar Producto"
1. El usuario completa el formulario y presiona *"Guardar Producto"*.
2. React verifica si el usuario está autenticado. Si lo está, toma su token JWT.
3. El frontend despacha un `POST /api/products` con el encabezado `Authorization: Bearer <token>`.
4. <mark>**Amazon API Gateway** verifica el token en el perímetro con el Cognito Authorizer</mark>.
5. Si es válido, lo envía a **Render**, donde **Spring Security** certifica la firma matemática con las claves JWKS de Amazon.
6. El controlador de Spring Boot invoca el repositorio JPA.
7. Hibernate ejecuta un `INSERT` en **Supabase PostgreSQL** a través del Session Pooler (puerto 5432).
8. El producto se guarda, el backend responde `HTTP 201 Created` y la tarjeta del nuevo producto aparece reactivamente en pantalla.

---

### 14. ¿Por qué usamos el Session Pooler (puerto 5432) en Supabase y no la Conexión Directa?
1. <mark>**El problema de IPv6 de Render:**</mark> La conexión directa de Supabase requiere IPv6, pero los servidores de Render solo tienen salida por IPv4. La conexión directa fallaría por tiempo de espera (*timeout*). El Pooler de Supabase soporta **IPv4**.
2. **Compatibilidad con Hibernate:** Spring Boot usa su propio gestor de conexiones (**HikariCP**). El modo Session en el puerto 5432 mantiene viva la conexión durante toda la transacción, evitando colisiones con las sentencias preparadas de Hibernate.

---

### 15. ¿Por qué es fundamental el uso de archivos `.gitignore` en ambos proyectos?
* <mark>**Buenas prácticas de ingeniería:**</mark> A los repositorios solo debe subirse el código fuente escrito por nosotros.
* No se deben subir carpetas de dependencias como `node_modules/` (en React) ni binarios compilados como `target/` (en Java), ya que Vercel y Render se encargan de descargarlos y construirlos de forma limpia en la nube.
* Evita la filtración accidental de claves y archivos temporales del sistema operativo.

---

# 🛡️ PARTE 5: TARJETA DE RESPUESTAS RÁPIDAS PARA PREGUNTAS DIFÍCILES

| Si el profesor pregunta... | Debes responder textualmente... |
| :--- | :--- |
| **"¿Dónde están las contraseñas de los usuarios en tu Supabase?"** | <mark>*"En ninguna parte, profesor. Por arquitectura de seguridad, nuestro sistema jamás toca ni almacena contraseñas. Toda la gestión de credenciales y hashes está delegada exclusivamente en AWS Cognito."*</mark> |
| **"¿El backend guarda sesiones de los usuarios en memoria?"** | <mark>*"No, es una arquitectura 100% Stateless. Cada petición viaja con su propio token JWT firmado. El backend no necesita recordar quién eres porque el token certifica tu identidad en cada llamada."*</mark> |
| **"¿Qué pasa si intercepto el token y le cambio el rol a ADMIN?"** | <mark>*"Al cambiar cualquier letra del token se rompe la firma digital (Signature). Cuando Spring Security o API Gateway comprueban la firma contra la clave pública de Amazon, detectan la inconsistencia y rechazan la llamada con HTTP 401 Unauthorized."*</mark> |
| **"¿Por qué usas API Gateway si ya tienes Render?"** | <mark>*"Para implementar defensa en profundidad y offloading: API Gateway actúa de escudo en el perímetro, filtrando tokens falsos antes de que consuman procesador o memoria en nuestro servidor de Render."*</mark> |
| **"¿Por qué usas PKCE si ya estás en HTTPS?"** | <mark>*"HTTPS protege los datos mientras viajan por internet, pero PKCE protege el código de autorización dentro del propio dispositivo del usuario para que extensiones o apps maliciosas no puedan robarse la sesión."*</mark> |

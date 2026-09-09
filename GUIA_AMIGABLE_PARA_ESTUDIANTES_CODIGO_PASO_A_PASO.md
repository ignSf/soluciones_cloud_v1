# Guía Amigable para Estudiantes: ¿Cómo funciona el código por dentro? 🎓

> **Para entender el proyecto sin enredos:** Esta guía explica con peras y manzanas qué hace cada archivo de código, por qué está escrito así y cómo puedes explicárselo a tu profesor con total confianza, sin memorizar tecnicismos difíciles.

---

## 💡 LA IDEA GENERAL EN 30 SEGUNDOS (Para entender todo el cuadro)

Imagina que nuestro proyecto es una **tienda exclusiva**:
1. **El Frontend (React en Vercel):** Es la vitrina bonita que el cliente ve en su pantalla.
2. **AWS Cognito:** Es la **oficina de pasaportes**. Nadie entra a comprar sin mostrar un pasaporte digital válido emitido por ellos.
3. **Amazon API Gateway:** Es el **guardia en la puerta de la calle**. Si no traes pasaporte, no te deja ni asomar la cabeza.
4. **El Backend (Spring Boot en Render):** Es el **administrador adentro del local**. Revisa con una lupa especial que el sello del pasaporte sea auténtico.
5. **Supabase (PostgreSQL):** Es la **bodega** donde se guardan las cajas de productos para que no se pierdan al apagar las luces.

---

# 🎨 PARTE 1: EL FRONTEND (REACT) EXPLICADO LÍNEA POR LÍNEA

---

### 1. El archivo de configuración: [authConfig.ts](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/frontend/src/auth/authConfig.ts)

*Este archivo es como la **ficha de contacto** con Amazon AWS. Le dice a React a dónde tiene que llamar cuando alguien quiere iniciar sesión.*

```typescript
// frontend/src/auth/authConfig.ts

// 1. La dirección web de la pantalla de login que nos regaló Amazon
const cognitoDomain = 'https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com';

export const cognitoAuthConfig: AuthProviderProps = {
  // 2. ¿Quién es la autoridad en la que confiamos? Nuestro User Pool de AWS
  authority: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',

  // 3. El identificador de nuestra app dentro de Amazon (como el RUT de la aplicación)
  client_id: '53acsgrc0tneq7jhigesj93g8r',

  // 4. ¿A dónde debe volver el usuario después de poner su clave en Amazon?
  // window.location.origin es un truco genial: detecta solito si estás en tu PC (localhost) o en Vercel
  redirect_uri: typeof window !== 'undefined' ? window.location.origin + '/' : 'https://soluciones-cloud-v1.vercel.app/',

  // 5. 'code' significa que queremos usar el flujo seguro moderno (PKCE)
  response_type: 'code',

  // 6. Los datos que le pedimos prestados a Amazon sobre el usuario
  scope: 'email openid phone',

  // 7. El mapa de direcciones de Amazon (dónde hacer login, dónde pedir el token, etc.)
  metadata: {
    issuer: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',
    authorization_endpoint: `${cognitoDomain}/login`,
    token_endpoint: `${cognitoDomain}/oauth2/token`,
    userinfo_endpoint: `${cognitoDomain}/oauth2/userInfo`,
    end_session_endpoint: `${cognitoDomain}/logout`,
    jwks_uri: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL/.well-known/jwks.json',
  },

  // 8. Una vez que Amazon nos devuelve el código en la URL, esta función limpia la barra del navegador
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};
```

> **¿Cómo explicárselo al profesor?**  
> *"Profesor, este archivo centraliza la configuración de AWS Cognito. Usamos `window.location.origin` para que el sistema reconozca automáticamente si estamos trabajando en desarrollo local o en producción en Vercel sin tener que cambiar el código a mano."*

---

### 2. La mochila global: [main.tsx](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/frontend/src/main.tsx)

*Para que cualquier botón de la página sepa si el usuario inició sesión o no, envolvemos toda la app en un "proveedor" (`AuthProvider`).*

```tsx
// frontend/src/main.tsx
<AuthProvider {...cognitoAuthConfig}>
  <App />
</AuthProvider>
```

> **¿Qué hace esto en palabras simples?**  
> Es como ponerle una mochila compartida a toda la página. Cualquier botón, formulario o menú ahora puede meter la mano a esa mochila y preguntar: *¿El usuario está conectado? ¿Cuál es su correo? ¿Dónde está su token?*.

---

### 3. El botón de Login y Logout: [Navbar.tsx](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/frontend/src/components/Navbar.tsx)

*Aquí vive el botón que inicia toda la magia.*

```tsx
// frontend/src/components/Navbar.tsx
const auth = useAuth(); // Sacamos las herramientas de la mochila

// CASO 1: Si ya inició sesión, mostramos su correo y el botón salir
if (auth.isAuthenticated) {
  return (
    <div>
      <span>👤 {auth.user?.profile.email}</span>
      <button onClick={() => auth.removeUser()}>Cerrar sesión</button>
    </div>
  );
}

// CASO 2: Si NO ha iniciado sesión, mostramos el botón para ir a Amazon
return (
  <button onClick={() => auth.signinRedirect()}>
    Iniciar sesión con Cognito
  </button>
);
```

> **¿Qué hace `auth.signinRedirect()`?**  
> Toma al usuario de la mano y lo lleva a la pantalla oficial de Amazon AWS para que ponga su correo y contraseña de forma 100% segura.

---

### 4. Meter el pasaporte en la carta: [productService.ts](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/frontend/src/services/productService.ts)

*Cuando queremos crear un producto, tenemos que mandar la carta al servidor con el pasaporte digital (JWT) adentro.*

```typescript
// frontend/src/services/productService.ts
async create(product: Product, token?: string): Promise<Product> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  // ¡AQUÍ ESTÁ EL SECRETO!
  // Si el usuario está logeado, le pegamos el token en la cabecera HTTP
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Despachamos la petición hacia Amazon API Gateway
  const response = await fetch('https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod/api/products', {
    method: 'POST',
    headers,
    body: JSON.stringify(product),
  });

  return response.json();
}
```

---

# ☕ PARTE 2: EL BACKEND (SPRING BOOT) EXPLICADO SIN DOLOR

---

### 1. El paquete que nos ahorró 300 líneas: [pom.xml](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/demo/pom.xml)

*Antes teníamos que programar filtros matemáticos a mano. Ahora usamos la librería oficial de Spring:*

```xml
<!-- demo/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

> **¿Qué hace esto en palabras simples?**  
> Le enseña a Spring Boot a hablar el idioma de los tokens JWT de Amazon de forma nativa. Ya no tenemos que inventar la rueda.

---

### 2. La línea mágica: [application.properties](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/demo/src/main/resources/application.properties)

*Le damos a Spring Boot la dirección de nuestro User Pool:*

```properties
# demo/src/main/resources/application.properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL
```

> **¿Qué pasa por debajo cuando el servidor arranca con esta línea?**  
> 1. Spring Boot se conecta a internet y le dice a Amazon: *"Hola, vine a buscar tus llaves públicas"*.
> 2. Amazon le entrega su archivo de claves públicas (`jwks.json`).
> 3. Spring Boot las guarda en su memoria RAM.
> 4. Ahora Spring Boot puede verificar cualquier token en 1 milisegundo sin tener que molestar a Amazon en cada clic.

---

### 3. Las 3 Reglas del Guardia: [SecurityConfig.java](file:///c:/Users/sours/OneDrive/Escritorio/Proyectos%20programacion/Nueva%20carpeta/demo/src/main/java/com/example/demo/config/SecurityConfig.java)

*Aquí le decimos a Spring Boot quién entra libre y a quién le pedimos pasaporte:*

```java
// demo/src/main/java/com/example/demo/config/SecurityConfig.java

// REGLA 1: No guardar sesiones en memoria (Arquitectura Stateless)
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

.authorizeHttpRequests(auth -> auth
    // REGLA 2: Ver el catálogo (GET) es PÚBLICO para todo el mundo
    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

    // REGLA 3: Guardar (POST) o Borrar (DELETE) exige TOKEN OBLIGATORIO
    .requestMatchers(HttpMethod.POST, "/api/products/**").authenticated()
    .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

    .anyRequest().authenticated()
)

// REGLA 4: Activar el lector automático de tokens JWT de Cognito
.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
```

---

# 🎬 PARTE 3: ¿QUÉ PASA CUANDO HAGO CLIC EN "GUARDAR PRODUCTO"? (En cámara lenta)

1. **En React:** Llenas el formulario y tocas "Guardar". React revisa si estás logeado. Si sí, saca tu token JWT (`Bearer eyJ...`).
2. **Viaje por internet:** La petición sale volando hacia la nube de Amazon.
3. **El guardia perimetral (Amazon API Gateway):**
   * Revisa que la petición traiga el token en la cabecera.
   * Si alguien intenta enviar una petición sin token, API Gateway le cierra la puerta en la cara con un error `401 Unauthorized`.
4. **La llegada a Render (Spring Boot):**
   * Como API Gateway lo dejó pasar, la petición llega a nuestro contenedor en Render.
   * Spring Security toma el token y le aplica la **fórmula matemática RS256** con la clave pública de Amazon.
   * Revisa que la firma no esté rota y que la fecha de expiración no haya pasado.
5. **El guardado en Supabase:**
   * Spring Boot le dice a PostgreSQL en Supabase: `INSERT INTO products (name, price...) VALUES (...)`.
   * Supabase lo guarda de forma permanente y le asigna su número de ID.
6. **La vuelta a casa:**
   * Spring Boot responde `201 Creado`.
   * React recibe el producto nuevo y lo dibuja al instante en la lista sin recargar la página.

---

# 🗣️ PARTE 4: CÓMO RESPONDERLE AL PROFESOR COMO UN CRACK (Sin ponerse nervioso)

### Pregunta 1: "¿Por qué usaron AWS Cognito en vez de guardar las contraseñas en su base de datos?"
> **Tu respuesta:**  
> *"Profesor, por un principio básico de seguridad moderna: **cero custodia de credenciales sensibles**. Si nosotros guardáramos contraseñas en Supabase, seríamos responsables si hay una filtración de datos. Al usar AWS Cognito como IDaaS, toda la responsabilidad de cifrado, fuerza bruta y seguridad recae en la infraestructura de Amazon. Nosotros solo validamos tokens."*

---

### Pregunta 2: "¿Qué significa que la arquitectura sea 'Stateless'?"
> **Tu respuesta:**  
> *"Significa 'sin estado', profesor. Nuestro backend en Render no guarda sesiones en memoria ni usa cookies para recordar al usuario. Cada petición viaja con su propio token JWT firmado. El token es autosuficiente: contiene la identidad del usuario y la firma de Amazon. Así, si tuviéramos 50 servidores en la nube, cualquiera podría atender la petición sin problemas."*

---

### Pregunta 3: "¿Qué es PKCE y por qué lo tuvieron que usar?"
> **Tu respuesta:**  
> *"PKCE significa 'Proof Key for Code Exchange'. Lo usamos porque nuestra aplicación de React corre en el navegador de cualquier persona, lo que significa que el código fuente es público. No podemos esconder una contraseña secreta del sistema (`client_secret`) en React porque cualquiera la vería inspeccionando el código. PKCE soluciona esto inventando una clave temporal secreta al vuelo para cada inicio de sesión."*

---

### Pregunta 4: "Si yo cambio mi rol a ADMIN en el token con jwt.io, ¿puedo engañar a tu servidor?"
> **Tu respuesta:**  
> *"No, profesor. El token tiene una tercera parte llamada **Signature (Firma)**. Esa firma fue calculada con la clave privada de Amazon sobre los datos originales. Si usted cambia aunque sea una sola letra en el token (de USER a ADMIN), la firma matemática se rompe. Cuando Spring Boot intente comprobar la firma con la clave pública de Amazon, el cálculo no coincidirá y el servidor rechazará la petición inmediatamente con un error 401 Unauthorized."*

---

### Pregunta 5: "¿Por qué pusieron un API Gateway si ya tenían el backend en Render?"
> **Tu respuesta:**  
> *"Por **defensa en profundidad y optimización de recursos (offloading)**. Si recibimos un ataque de peticiones falsas o sin token, el API Gateway las frena en el borde de la red de Amazon. De esa manera, nuestro servidor en Render ni siquiera se entera del ataque y no gasta memoria, procesador ni dinero atendiendo peticiones basura."*

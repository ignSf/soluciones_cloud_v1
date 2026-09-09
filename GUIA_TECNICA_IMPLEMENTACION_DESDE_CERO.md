# Guía Técnica de Ingeniería: Implementación de AWS Cognito y Spring Security desde Cero (Sin IA)

> **Documento de Arquitectura y Construcción de Software**  
> **Objetivo:** Manual de referencia técnica paso a paso para un desarrollador que debe implementar la autenticación con AWS Cognito en Frontend (React) y Backend (Spring Boot) desde una terminal vacía, detallando qué parámetros entrega Amazon, en qué archivos exactos se colocan y cómo interactúan las librerías.

---

## 🗺️ MAPA GENERAL DE DATOS: QUÉ ENTREGA COGNITO Y DÓNDE VA

Cuando un ingeniero crea un **User Pool** en AWS Cognito, la consola de Amazon le entrega **4 valores fundamentales**. Esta tabla resume exactamente en qué archivo del código fuente se inyecta cada uno:

| Dato entregado por AWS Cognito | Ejemplo Real del Proyecto | ¿En qué archivo de código se implementa? |
| :--- | :--- | :--- |
| **1. Región de AWS** | `us-east-1` | Compone las URLs en Frontend y Backend. |
| **2. User Pool ID** | `us-east-1_OL9DjB9XL` | Frontend (`authConfig.ts`) y Backend (`application.properties`). |
| **3. App Client ID** | `53acsgrc0tneq7jhigesj93g8r` | Frontend (`authConfig.ts`) y Cognito Callback URLs. |
| **4. Dominio de Hosted UI** | `https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com` | Frontend (`authConfig.ts`) para los endpoints OAuth2. |

---

# 📦 FASE 1: IMPLEMENTACIÓN EN EL FRONTEND (REACT + TYPESCRIPT)

### Paso 1.1: Instalación de las Bibliotecas OIDC
En una aplicación de React, no se programa el protocolo OAuth2 ni la matemática de PKCE desde cero. Se instalan las librerías oficiales y certificadas por la OpenID Foundation:

```bash
cd frontend
npm install react-oidc-context oidc-client-ts
```

* **`oidc-client-ts`:** Es el motor criptográfico en TypeScript que genera el `code_verifier`, calcula el `code_challenge` (SHA-256), procesa la redirección y almacena los tokens en memoria/storage.
* **`react-oidc-context`:** Es el adaptador para React que provee el contexto global (`<AuthProvider>`) y los React Hooks (`useAuth()`).

---

### Paso 1.2: Archivo de Configuración de Cognito (`src/auth/authConfig.ts`)
Aquí es donde se ensamblan los 4 valores obtenidos de la consola de AWS. Se crea un archivo dedicado para desacoplar las credenciales del resto de la interfaz:

```typescript
// Archivo: frontend/src/auth/authConfig.ts
import type { AuthProviderProps } from 'react-oidc-context';

// 1. Dominio que creamos en la pestaña "Creación de marca -> Dominio" de Cognito
const cognitoDomain = 'https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com';

export const cognitoAuthConfig: AuthProviderProps = {
  // 2. Authority: URL única del User Pool (Issuer). Amazon publica aquí los metadatos OIDC
  authority: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',

  // 3. Client ID: Identificador público de la aplicación (creado SIN client secret)
  client_id: '53acsgrc0tneq7jhigesj93g8r',

  // 4. Redirect URI: A dónde debe volver el navegador tras ingresar la clave en AWS
  // Usar window.location.origin permite que funcione automáticamente en localhost o en Vercel
  redirect_uri: typeof window !== 'undefined' ? window.location.origin + '/' : 'https://soluciones-cloud-v1.vercel.app/',

  // 5. Response Type: 'code' activa obligatoriamente el flujo seguro Authorization Code + PKCE
  response_type: 'code',

  // 6. Scopes: Qué información le solicitamos a Cognito sobre el usuario
  scope: 'email openid phone',

  // 7. Metadata: Los endpoints exactos donde la librería buscará los servicios de AWS
  metadata: {
    issuer: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',
    authorization_endpoint: `${cognitoDomain}/login`,
    token_endpoint: `${cognitoDomain}/oauth2/token`,
    userinfo_endpoint: `${cognitoDomain}/oauth2/userInfo`,
    end_session_endpoint: `${cognitoDomain}/logout`,
    jwks_uri: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL/.well-known/jwks.json',
  },

  // 8. Callback para limpiar la URL del navegador una vez extraído el '?code=...'
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};
```

---

### Paso 1.3: Envolver la Aplicación (`src/main.tsx`)
Para que cualquier botón o componente de React sepa si el usuario inició sesión o no, se envuelve el árbol de componentes con el proveedor:

```tsx
// Archivo: frontend/src/main.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider } from 'react-oidc-context';
import { cognitoAuthConfig } from './auth/authConfig';
import { App } from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    {/* Inyecta el estado de Cognito a toda la aplicación */}
    <AuthProvider {...cognitoAuthConfig}>
      <App />
    </AuthProvider>
  </React.StrictMode>
);
```

---

### Paso 1.4: Gestionar el Botón de Login/Logout (`src/components/Navbar.tsx`)
En cualquier componente donde se requiera iniciar o cerrar sesión, se consume el hook `useAuth()`:

```tsx
// Archivo: frontend/src/components/Navbar.tsx
import { useAuth } from 'react-oidc-context';

export const Navbar = () => {
  const auth = useAuth();

  if (auth.isLoading) {
    return <span>Comprobando sesión...</span>;
  }

  if (auth.isAuthenticated) {
    return (
      <div>
        {/* Cognito entrega los datos del usuario en auth.user.profile */}
        <span>Hola, {auth.user?.profile.email}</span>
        <button onClick={() => auth.removeUser()}>Cerrar sesión</button>
      </div>
    );
  }

  // Redirige hacia la Hosted UI de Amazon Cognito usando PKCE
  return (
    <button onClick={() => auth.signinRedirect()}>
      Iniciar sesión con Cognito
    </button>
  );
};
```

---

### Paso 1.5: Extracción del JWT y Envío al Backend (`src/services/productService.ts`)
Cuando se realiza una petición que requiere autenticación (ej: `POST` o `DELETE`), se extrae el token y se inyecta en la cabecera `Authorization`:

```typescript
// Archivo: frontend/src/services/productService.ts
export const productService = {
  async create(product: Product, token?: string): Promise<Product> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    // Si el usuario está logeado, adjuntamos el JWT como Bearer token
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch('https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod/api/products', {
      method: 'POST',
      headers,
      body: JSON.stringify(product),
    });

    if (response.status === 401) {
      throw new Error('No autorizado: Token inválido o sesión expirada.');
    }

    return response.json();
  }
};
```

---

# ☕ FASE 2: IMPLEMENTACIÓN EN EL BACKEND (SPRING BOOT / JAVA)

En el backend, Spring Boot actúa como un **Resource Server (Servidor de Recursos)**. El backend **no tiene conexión directa a la base de datos de usuarios de Cognito**, ni le pregunta a Cognito por usuario y contraseña. Solo verifica firmas matemáticas de los tokens que le llegan.

---

### Paso 2.1: Dependencia Maven Oficial (`pom.xml`)
En lugar de escribir algoritmos criptográficos a mano con librerías de bajo nivel, se agrega el módulo estándar de Spring Security para servidores de recursos OAuth2:

```xml
<!-- Archivo: demo/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Esta sola dependencia incluye:
1. Validadores de tokens JWT.
2. Soporte para el algoritmo asimétrico **RS256**.
3. Descubridor automático de claves públicas mediante el protocolo OpenID Connect.
4. El filtro interceptor `BearerTokenAuthenticationFilter`.

---

### Paso 2.2: Enlazar el Emisor de Cognito (`application.properties`)
Spring Boot necesita saber **quién es el emisor legítimo** de los tokens. Solo se requiere una propiedad:

```properties
# Archivo: demo/src/main/resources/application.properties

# Issuer URI: URL de nuestro User Pool en AWS Cognito
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL
```

#### ¿Qué hace Spring Boot con esta propiedad por debajo?
1. Al arrancar, Spring Boot toma esa URL y le concatena `/.well-known/openid-configuration`.
2. Hace una llamada HTTP hacia Amazon y obtiene un documento JSON que le dice: *"Mis claves públicas están en `/us-east-1_OL9DjB9XL/.well-known/jwks.json`"*.
3. Descarga las claves públicas (JWKS) y las guarda en memoria caché.
4. Queda listo para validar cualquier token sin tener que llamar a Amazon en cada petición.

---

### Paso 2.3: La Cadena de Filtros de Seguridad (`SecurityConfig.java`)
Aquí se definen qué rutas son públicas y cuáles exigen el token firmado:

```java
// Archivo: demo/src/main/java/com/example/demo/config/SecurityConfig.java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Habilitar CORS para permitir llamadas desde Vercel o API Gateway
            .cors(Customizer.withDefaults())

            // 2. Desactivar CSRF: En APIs REST sin cookies de sesión no aplica ni se requiere
            .csrf(AbstractHttpConfigurer::disable)

            // 3. Sesión STATELESS: No guardar sesiones en memoria RAM del servidor
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Reglas de Autorización de Endpoints
            .authorizeHttpRequests(auth -> auth
                // Peticiones de sondeo del navegador (Preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // GET /api/products es PÚBLICO: cualquiera puede ver el catálogo
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // POST y DELETE son PROTEGIDOS: exigen token JWT válido de Cognito
                .requestMatchers(HttpMethod.POST, "/api/products/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )

            // 5. ACTIVAR EL RESOURCE SERVER NATIVO PARA VALIDAR JWT AUTOMÁTICAMENTE
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
```

---

# 🔬 FASE 3: QUÉ SUCEDE PASO A PASO EN EL CÓDIGO CUANDO LLEGA UNA PETICIÓN

Para explicar el funcionamiento interno en una evaluación, describa esta secuencia de eventos:

```
[ Petición HTTP ] ➔ [ 1. BearerTokenAuthenticationFilter ]
                               │
                               ▼
                    [ 2. NimbusJwtDecoder ] ◄── Descarga JWKS de Cognito
                               │
                               ├─► ¿Firma RS256 válida con clave pública? (Sí / No)
                               ├─► ¿iss == User Pool ID? (Sí / No)
                               └─► ¿exp > fecha actual? (Sí / No)
                               │
                ┌──────────────┴──────────────┐
             [ Válido ]                   [ Inválido ]
                │                              │
                ▼                              ▼
    [ SecurityContextHolder ]         [ HTTP 401 Unauthorized ]
    (Usuario autenticado)             (Bloqueo de seguridad)
                │
                ▼
      [ ProductController ]
      (Ejecuta método create)
```

1. **Llegada del Header:** La petición llega con `Authorization: Bearer eyJhbGci...`.
2. **Extracción:** El filtro de Spring Security extrae la cadena de texto después de la palabra `Bearer `.
3. **Decodificación del Header:** Spring lee el `kid` (*Key ID*) del token para saber cuál de las claves públicas de Cognito se usó para firmarlo.
4. **Validación Criptográfica:** Aplica el algoritmo matemático **RS256** utilizando la clave pública correspondiente. Si un atacante modificó el rol de `USER` a `ADMIN` en el Payload, la firma no coincide y Spring lanza una excepción `BadJwtException`.
5. **Validación Temporal:** Comprueba que el claim `exp` (expiración) sea superior al tiempo actual del servidor.
6. **Inyección en Contexto:** Si todo es correcto, Spring instancia un objeto `JwtAuthenticationToken` y lo almacena en el `SecurityContextHolder`.
7. **Paso al Controlador:** El método `createProduct` en `ProductController.java` se ejecuta y guarda los datos en **Supabase**.

---

# 🚀 FASE 4: CONFIGURACIONES OBLIGATORIAS EN AWS COGNITO (Para que no falle)

Si un programador olvida alguno de estos puntos en la consola de AWS, el código fallará con errores de redirección o CORS:

1. **En el App Client:**
   * **Desactivar el Client Secret:** En la creación del cliente, desmarcar *"Generar un secreto de cliente"*. Si se deja activo, el frontend en React fallará porque no tiene forma segura de enviar el secret.
2. **En Allowed Callback URLs:**
   * Escribir exactamente la URL donde corre la web (ej: `https://soluciones-cloud-v1.vercel.app/` y `http://localhost:5173/`). Si falta la barra final `/` o hay una letra distinta, Amazon arrojará el error: `redirect_mismatch`.
3. **En la Experiencia de Registro:**
   * Activar el envío automático de correos de verificación para que el usuario reciba su código de 6 dígitos y al ingresarlo la cuenta quede en estado `CONFIRMED` en automático.

---

# 📌 GUION DE RESPUESTA DIRECTA PARA LA EVALUACIÓN

Si el profesor le pregunta:
> *"Explíqueme técnicamente cómo implementó Cognito en su código y qué cambios tuvo que hacer en el backend y en el frontend."*

**Respuesta de nivel senior:**
> *"Profesor, en el **Frontend (React)** instalamos la librería certificada `react-oidc-context`. Creamos un archivo `authConfig.ts` donde mapeamos el **User Pool ID**, el **App Client ID** y el **Dominio de la Hosted UI** de AWS. Configuramos el flujo seguro **Authorization Code con PKCE** para clientes públicos. Al iniciar sesión, React obtiene los tokens JWT y los adjunta en la cabecera HTTP `Authorization: Bearer <token>` de cada petición protegida.*
> 
> *En el **Backend (Spring Boot)**, no programamos validaciones manuales. Agregamos el starter oficial `spring-boot-starter-oauth2-resource-server` y en `application.properties` configuramos la propiedad `spring.security.oauth2.resourceserver.jwt.issuer-uri` apuntando a nuestro User Pool. Spring Security se encarga automáticamente de consultar el endpoint `jwks.json` de Amazon en el arranque, descargar las claves públicas y verificar la firma criptográfica asimétrica (RS256) de cada petición antes de permitir el acceso al controlador y a la base de datos Supabase."*

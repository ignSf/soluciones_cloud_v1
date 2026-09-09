# 📄 INFORME TÉCNICO: EVALUACIÓN PARCIAL N° 1
## Asignatura: Desarrollo Cloud Native I (DSY1107)
### Duoc UC — Escuela de Informática y Telecomunicaciones

---

| Campo | Detalle |
| :--- | :--- |
| **Sigla / Asignatura** | DSY1107 — Desarrollo Cloud Native I |
| **Evaluación** | Evaluación Parcial N° 1 (Ponderación: 16% / 40% del ET) |
| **Título del Proyecto** | Arquitectura Cloud Native Segura: Gestión de Productos con IDaaS (AWS Cognito), Amazon API Gateway, Spring Boot y React |
| **Integrantes** | **Ignacio Salazar**<br>**Cristofer Cifuentes** |
| **Fecha de Entrega** | Septiembre 2025 / 2026 |
| **Repositorio GitHub** | [https://github.com/ignSf/soluciones_cloud_v1](https://github.com/ignSf/soluciones_cloud_v1) |
| **Frontend en Producción** | [https://soluciones-cloud-v1.vercel.app](https://soluciones-cloud-v1.vercel.app) |
| **Backend en Producción** | [https://soluciones-cloud-v1.onrender.com](https://soluciones-cloud-v1.onrender.com) |
| **Endpoint API Gateway** | [https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod/api/products](https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod/api/products) |

---

## 📑 ÍNDICE GENERAL

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Introducción y Objetivos](#2-introducción-y-objetivos)
3. [Arquitectura General del Sistema y Topología Cloud](#3-arquitectura-general-del-sistema-y-topología-cloud)
4. [Implementación del Proveedor de Identidad (IDaaS) con AWS Cognito](#4-implementación-del-proveedor-de-identidad-idaas-con-aws-cognito)
5. [Desarrollo del Frontend (React + TypeScript + Vite + PKCE)](#5-desarrollo-del-frontend-react--typescript--vite--pkce)
6. [Puerta de Enlace Perimetral: Amazon API Gateway](#6-puerta-de-enlace-perimetral-amazon-api-gateway)
7. [Desarrollo del Backend / BFF (Spring Boot 3 + Spring Security + OAuth2)](#7-desarrollo-del-backend--bff-spring-boot-3--spring-security--oauth2)
8. [Persistencia Cloud: PostgreSQL en Supabase](#8-persistencia-cloud-postgresql-en-supabase)
9. [Flujos de Seguridad y Criptografía de Tokens (JWT)](#9-flujos-de-seguridad-y-criptografía-de-tokens-jwt)
10. [Evidencias de Pruebas y Validación de Seguridad](#10-evidencias-de-pruebas-y-validación-de-seguridad)
11. [Conclusiones](#11-conclusiones)

---

## 1. RESUMEN EJECUTIVO

El presente informe documenta el diseño, desarrollo, aseguramiento y despliegue de la solución integral de software correspondiente a la **Evaluación Parcial N° 1** de la asignatura **Desarrollo Cloud Native I**.

El proyecto implementa una arquitectura desacoplada, escalable y sin estado (*Stateless*), orientada a microservicios y servicios gestionados en la nube. La seguridad y gestión de identidades se delega en un proveedor **IDaaS (Identity as a Service)** de clase empresarial mediante **AWS Cognito**, implementando el estándar **OAuth 2.0** con **OpenID Connect (OIDC)** y el protocolo de intercambio criptográfico **PKCE (Proof Key for Code Exchange)**. 

El tráfico de red hacia la API es administrado y filtrado perimetralmente mediante **Amazon API Gateway** (con integración HTTP Proxy y control de CORS). La lógica de negocio está construida sobre **Spring Boot 3.4** en **Java 17**, configurado formalmente como un **OAuth2 Resource Server** capaz de validar tokens JWT de manera asimétrica mediante el conjunto de llaves públicas **JWKS** de Amazon. La persistencia se delega en un clúster relacional de **PostgreSQL en Supabase** conectado a través de un **Session Pooler (puerto 5432)**, y ambos componentes de software han sido desplegados y automatizados en entornos Cloud de alta disponibilidad (**Vercel** para el Frontend y **Render** para el Backend).

---

## 2. INTRODUCCIÓN Y OBJETIVOS

### 2.1 Contexto del Problema
En el desarrollo de aplicaciones modernas bajo el paradigma *Cloud Native*, la gestión interna de credenciales, sesiones en memoria y bases de datos monolíticas representa un alto riesgo de seguridad (vulnerabilidades OWASP, exposición de contraseñas, fallas de cumplimiento normativo) y un cuello de botella de rendimiento.

Para mitigar estos problemas, este proyecto adopta los lineamientos de la industria financiera y corporativa:
* Delegar la autenticación en un IDaaS externo especializado.
* Eliminar el estado en el backend (*Stateless Session Management*).
* Desacoplar el acceso a los datos mediante tokens criptográficamente firmados (JWT).
* Proteger los endpoints con una puerta de enlace (*API Gateway*) perimetral.

### 2.2 Objetivos del Proyecto
* **Objetivo General:** Diseñar e implementar una solución Cloud Native completamente funcional y segura para la administración de productos e inventario, demostrando la comunicación eficiente y protegida entre cliente web, IDaaS, API Gateway, Backend y Base de Datos Cloud.
* **Objetivos Específicos:**
  1. Configurar un User Pool y App Client en **AWS Cognito** sin secreto de cliente, habilitando PKCE para aplicaciones SPA.
  2. Desarrollar una interfaz de usuario modular y responsiva en **React + TypeScript**, integrando la librería de autenticación OIDC para login, logout y captura de tokens.
  3. Desplegar un **API Gateway** en AWS con métodos REST, control de cabeceras CORS e integración proxy hacia el servidor de aplicaciones.
  4. Configurar **Spring Security** en Spring Boot como Resource Server para interceptar y validar automáticamente tokens Bearer JWT vía JWKS.
  5. Integrar el ORM Hibernate y Spring Data JPA con **Supabase PostgreSQL** utilizando un mecanismo de pool de conexiones para alta concurrencia.
  6. Desplegar el sistema en plataformas Cloud automatizadas con certificados SSL/TLS y control de versiones en **GitHub**.

---

## 3. ARQUITECTURA GENERAL DEL SISTEMA Y TOPOLOGÍA CLOUD

La arquitectura del sistema sigue el patrón **Desacoplado Cloud Native**, dividiendo las responsabilidades en capas claramente aisladas:

```mermaid
graph TB
    subgraph "CLIENT TIER"
        User["👤 Usuario / Navegador"]
        SPA["⚛️ Frontend SPA (React 19 + Vite)<br/>Hosting: Vercel Cloud (CDN)"]
    end

    subgraph "IDENTITY PROVIDER (IDaaS)"
        Cognito["🏛️ AWS Cognito User Pool<br/>OAuth 2.0 + OIDC + PKCE<br/>Región: us-east-1"]
    end

    subgraph "PERIMETER & INGRESS TIER"
        APIGW["🛡️ Amazon API Gateway<br/>REST API (Stage: prod)<br/>CORS Filter + HTTP Proxy Integration"]
    end

    subgraph "APPLICATION TIER (BFF / Microservice)"
        Backend["⚙️ Backend Spring Boot 3.4 (Java 17)<br/>Spring Security OAuth2 Resource Server<br/>Hosting: Render Cloud (Containerized)"]
    end

    subgraph "DATA PERSISTENCE TIER"
        DB["🗄️ Supabase PostgreSQL<br/>Session Pooler (Port 5432)<br/>AWS us-east-1"]
    end

    User -->|1. Navega en la aplicación| SPA
    SPA -->|2. Inicia sesión con PKCE| Cognito
    Cognito -- "3. Emite JWT (id_token + access_token)" --> SPA
    SPA -->|4. Petición HTTP + Authorization: Bearer JWT| APIGW
    APIGW -->|5. Proxy seguro HTTP| Backend
    Backend -.->|6. Valida firma asimétrica (JWKS)| Cognito
    Backend -->|7. Consultas JPA / SQL| DB
```

### Tabla de Componentes y Responsabilidades

| Componente | Entorno Cloud | Tecnología | Rol Principal |
| :--- | :--- | :--- | :--- |
| **Frontend Web** | Vercel (Edge CDN) | React 19, TypeScript, Vite, CSS moderno | Renderizado visual, inicio de sesión OIDC, almacenamiento en memoria del token y consumo de APIs. |
| **Proveedor IDaaS** | AWS (us-east-1) | AWS Cognito User Pool | Almacén de identidades, autenticación segura, emisión de JWTs firmados con RS256. |
| **API Gateway** | AWS (us-east-1) | Amazon API Gateway | Puerta de entrada perimetral, control de políticas CORS y reenvío por proxy. |
| **Backend / BFF** | Render Platform | Spring Boot 3.4, Java 17, Spring Security | Lógica de negocio, validación criptográfica de JWTs, exposición de endpoints REST. |
| **Base de Datos** | Supabase (AWS us-east-1) | PostgreSQL 15, PgBouncer | Almacenamiento persistente de productos mediante Session Pooler (puerto 5432). |

---

## 4. IMPLEMENTACIÓN DEL PROVEEDOR DE IDENTIDAD (IDaaS) CON AWS COGNITO

En cumplimiento de las especificaciones del encargo (sustituyendo Azure AD por **AWS Cognito**), se configuró un ecosistema de identidad administrado:

### 4.1 Parámetros de Configuración del User Pool
* **Nombre del User Pool:** `us-east-1_OL9DjB9XL`
* **Región:** `us-east-1` (EE.UU. Este - N. Virginia)
* **Atributo de Inicio de Sesión:** Correo electrónico (`email`) verificado.
* **Políticas de Contraseña:** Mínimo 8 caracteres, números, mayúsculas, minúsculas y caracteres especiales.

### 4.2 Configuración del App Client (Cliente de Aplicación)
* **App Client ID:** `53acsgrc0tneq7jhigesj93g8r`
* **Tipo de Cliente:** **Public Client** (*Generate client secret = FALSE*). Al tratarse de una Single Page Application (SPA), el código fuente reside en el navegador del cliente; por tanto, no es seguro almacenar un secreto estático.
* **Flujo OAuth Permitido:** **Authorization Code Grant** combinado obligatoriamente con **PKCE**.
* **Scopes Habilitados:** `openid`, `email`, `phone`.
* **Hosted UI Domain:** `https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com`
* **URLs de Retorno Autorizadas (Callback URLs):**
  * `http://localhost:5173/` (Desarrollo local con Vite)
  * `https://soluciones-cloud-v1.vercel.app/` (Producción en Vercel)
* **URLs de Cierre de Sesión (Sign-out URLs):**
  * `http://localhost:5173/`
  * `https://soluciones-cloud-v1.vercel.app/`

---

## 5. DESARROLLO DEL FRONTEND (REACT + TYPESCRIPT + VITE + PKCE)

El frontend fue desarrollado bajo una arquitectura modular y reactiva, evitando dependencias propietarias rígidas y adoptando estándares abiertos mediante las librerías `react-oidc-context` y `oidc-client-ts`.

### 5.1 Centralización de Configuración: `authConfig.ts`
El archivo `frontend/src/auth/authConfig.ts` implementa una solución dinámica para los entornos de desarrollo y producción mediante `window.location.origin`:

```typescript
// frontend/src/auth/authConfig.ts
import { AuthProviderProps } from 'react-oidc-context';

const cognitoDomain = 'https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com';

export const cognitoAuthConfig: AuthProviderProps = {
  authority: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',
  client_id: '53acsgrc0tneq7jhigesj93g8r',
  redirect_uri: typeof window !== 'undefined' ? window.location.origin + '/' : 'https://soluciones-cloud-v1.vercel.app/',
  response_type: 'code',
  scope: 'email openid phone',
  metadata: {
    issuer: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',
    authorization_endpoint: `${cognitoDomain}/login`,
    token_endpoint: `${cognitoDomain}/oauth2/token`,
    userinfo_endpoint: `${cognitoDomain}/oauth2/userInfo`,
    end_session_endpoint: `${cognitoDomain}/logout`,
    jwks_uri: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL/.well-known/jwks.json',
  },
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};
```

### 5.2 Proveedor Global de Contexto: `main.tsx`
Toda la jerarquía de componentes queda suscrita al contexto de autenticación mediante el componente `<AuthProvider>`:

```tsx
// frontend/src/main.tsx
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...cognitoAuthConfig}>
      <App />
    </AuthProvider>
  </StrictMode>
);
```

### 5.3 Interceptor y Consumo Seguro del Backend: `productService.ts`
Cada llamada HTTP dirigida al API Gateway inyecta automáticamente el token de acceso obtenido de Cognito:

```typescript
// frontend/src/services/productService.ts
const API_URL = 'https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod/api/products';

const getAuthHeaders = (token?: string): HeadersInit => {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
};

export const productService = {
  async getAll(token?: string): Promise<Product[]> {
    const response = await fetch(API_URL, { headers: getAuthHeaders(token) });
    if (!response.ok) throw new Error('Error al cargar productos');
    return response.json();
  },

  async create(product: CreateProductDTO, token: string): Promise<Product> {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(token),
      body: JSON.stringify(product),
    });
    if (!response.ok) throw new Error('Error al crear producto');
    return response.json();
  }
};
```

---

## 6. PUERTA DE ENLACE PERIMETRAL: AMAZON API GATEWAY

Para satisfacer el requerimiento de protección perimetral e integración Cloud Native, se configuró una API REST en **Amazon API Gateway**:

* **Identificador de API:** `8086dx45a7`
* **Nombre de Recurso:** `/api/products`
* **Etapa de Despliegue (Stage):** `prod`
* **URL Pública:** `https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod/api/products`
* **Mecanismo de Integración:** **HTTP Proxy Integration** apuntando al backend en Render:
  `https://soluciones-cloud-v1.onrender.com/api/products`
* **Configuración CORS Perimetral:**
  * Métodos autorizados: `GET`, `POST`, `OPTIONS`.
  * Cabeceras permitidas: `Content-Type`, `X-Amz-Date`, `Authorization`, `X-Api-Key`, `X-Amz-Security-Token`.
  * Origen permitido: `*` (Permitiendo peticiones desde Vercel y localhost).

---

## 7. DESARROLLO DEL BACKEND / BFF (SPRING BOOT 3 + SPRING SECURITY + OAUTH2)

El backend actúa como un **BFF (Backend for Frontend)** y Resource Server seguro, desarrollado en **Java 17** y **Spring Boot 3.4.3**.

### 7.1 Dependencias Cero Código Propietario (`pom.xml`)
Se integró el starter oficial de seguridad de Spring:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### 7.2 Configuración de Seguridad Perimetral: `SecurityConfig.java`
La clase `SecurityConfig` define las reglas de autorización, política de sesión sin estado y la validación de tokens:

```java
package com.example.soluciones_cloud_v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/health/**", "/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### 7.3 Configuración de Conexión: `application.yml`
Spring Boot descarga y sincroniza las llaves públicas de Amazon de manera automática mediante el parámetro `issuer-uri`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require}
    username: ${SPRING_DATASOURCE_USERNAME:postgres.xuffliucihabnrodubse}
    password: ${SPRING_DATASOURCE_PASSWORD:87749584aA.}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

---

## 8. PERSISTENCIA CLOUD: POSTGRESQL EN SUPABASE

La base de datos relacional se aloja en **Supabase** (bajo infraestructura física de AWS en `us-east-1`).

### 8.1 Esquema DDL (`supabase_schema.sql`)
```sql
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    category VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### 8.2 Decisión de Ingeniería: Session Pooler (Puerto 5432)
* **Problema identificado:** Las conexiones directas de Supabase dependen de redes IPv6, lo que genera fallos de resolución de DNS en contenedores de Render y plataformas cloud que operan bajo IPv4.
* **Solución implementada:** Se utilizó el **Session Pooler** de Supabase a través del host `aws-0-us-east-1.pooler.supabase.com` en el puerto estándar `5432`. Esto provee soporte nativo IPv4 y optimiza el consumo de memoria mediante el gestor PgBouncer integrado con el pool HikariCP de Spring Boot.

---

## 9. FLUJOS DE SEGURIDAD Y CRIPTOGRAFÍA DE TOKENS (JWT)

### 9.1 Estructura del JWT Emitido por Cognito
El token de acceso emitido por AWS Cognito cuenta con tres partes en base64url:
1. **Header:** Define el algoritmo (`"alg": "RS256"`) y el identificador de la llave pública de Amazon (`"kid"`).
2. **Payload:** Reclamaciones estándar y privadas (`sub`, `iss`, `client_id`, `exp`, `token_use: "access"`, `cognito:groups`).
3. **Signature:** Firma matemática generada mediante la **llave privada** de Amazon.

### 9.2 Validación Asimétrica sin Estado (Stateless)
El backend de Spring Boot no realiza llamadas HTTP a Cognito por cada petición que recibe:
1. Al arrancar, consulta una única vez el endpoint público JWKS:
   `https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL/.well-known/jwks.json`
2. Almacena en memoria las llaves públicas RSA.
3. Cuando llega una petición, el filtro `BearerTokenAuthenticationFilter` extrae el `kid` del token recibido y valida matemáticamente la firma utilizando la llave pública local.
4. Si la firma es válida, el emisor coincide (`iss`) y la fecha actual es menor que `exp`, la petición es admitida. En caso contrario, se emite un código HTTP `401 Unauthorized`.

---

## 10. EVIDENCIAS DE PRUEBAS Y VALIDACIÓN DE SEGURIDAD

| Caso de Prueba | Entrada / Acción | Resultado Esperado | Resultado Obtenido | Estado |
| :--- | :--- | :--- | :--- | :---: |
| **CP-01: Acceso Anónimo a Endpoint Protegido** | `GET https://8086dx45a7.../api/products` sin cabecera Authorization. | HTTP 401 Unauthorized | HTTP 401 Unauthorized (rechazado en Spring Security) | **APROBADO** |
| **CP-02: Endpoint Público de Salud** | `GET https://soluciones-cloud-v1.onrender.com/api/health` sin token. | HTTP 200 OK | HTTP 200 OK (`{"status":"UP"}`) | **APROBADO** |
| **CP-03: Token Manipulado Fraudulentamente** | Modificación del payload en jwt.io alterando un claim sin clave privada. | HTTP 401 Unauthorized | Fallo en la verificación de firma criptográfica RS256. Acceso denegado. | **APROBADO** |
| **CP-04: Login con PKCE en Cognito** | Inicio de sesión en Hosted UI de Cognito con usuario verificado. | Redirección con código y obtención de Access Token. | Token recibido y guardado en memoria en React. Navbar muestra email. | **APROBADO** |
| **CP-05: Creación de Producto Autenticado** | `POST /api/products` con token válido en `Authorization: Bearer`. | HTTP 201 Created y guardado en BD. | Producto persistido en Supabase PostgreSQL y renderizado en UI. | **APROBADO** |
| **CP-06: Pre-flight CORS** | Petición HTTP `OPTIONS` enviada por el navegador. | HTTP 200 OK con cabeceras `Access-Control-Allow-*`. | Petición aprobada por API Gateway y Spring Boot. | **APROBADO** |

---

## 11. CONCLUSIONES

1. **Adopción exitosa de Estándares de la Industria:** La implementación de OAuth 2.0 con PKCE en el frontend y un Resource Server sin estado en Spring Boot demuestra que no es necesario reinventar mecanismos propietarios de autenticación. Utilizar estándares abiertos asegura compatibilidad, robustez y protección contra ataques comunes.
2. **Seguridad Integral Delegada:** Al transferir la gestión de credenciales a AWS Cognito, el sistema garantiza el cumplimiento de normativas de privacidad y elimina la superficie de ataque asociada al almacenamiento de contraseñas locales.
3. **Escalabilidad y Alta Disponibilidad:** La combinación de Vercel (distribución CDN en el borde), Amazon API Gateway (enrutamiento elástico), Render (contenedores Java con auto-recuperación) y Supabase (PostgreSQL con connection pool) constituye una arquitectura Cloud Native moderna, altamente resiliente y preparada para cargas masivas de usuarios con costo operativo mínimo.
4. **Validación del Encargo:** El sistema se encuentra 100% operativo en producción, con código fuente versionado en GitHub, compilación limpia sin errores y documentación técnica exhaustiva para su defensa oral.

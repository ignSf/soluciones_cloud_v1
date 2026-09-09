# Technical Engineering Guide: Step-by-Step Implementation of AWS Cognito & Spring Security from Scratch

> **Software Architecture & Engineering Manual**  
> **Objective:** A comprehensive technical manual for developers implementing cloud authentication with AWS Cognito across Frontend (React + TypeScript) and Backend (Spring Boot / Java 21) from scratch, detailing what AWS Cognito delivers, exactly where each parameter is placed in the code, and how the underlying cryptographic libraries interact.

---

## 🗺️ DATA MAPPING: WHAT COGNITO DELIVERS & WHERE IT GOES

When creating a **User Pool** in AWS Cognito, the Amazon Web Services console provides **4 critical parameters**. This table maps each parameter to its exact destination in the source code:

| Parameter Provided by AWS Cognito | Production Value Example | Destination File & Property |
| :--- | :--- | :--- |
| **1. AWS Region** | `us-east-1` | Used across endpoint URLs in Frontend and Backend. |
| **2. User Pool ID** | `us-east-1_OL9DjB9XL` | Frontend (`authConfig.ts`) and Backend (`application.properties`). |
| **3. App Client ID** | `53acsgrc0tneq7jhigesj93g8r` | Frontend (`authConfig.ts`) and Cognito Callback URLs. |
| **4. Hosted UI Domain** | `https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com` | Frontend (`authConfig.ts`) to construct OAuth2 endpoints. |

---

# 📦 PHASE 1: FRONTEND IMPLEMENTATION (REACT + TYPESCRIPT)

### Step 1.1: Install Standard OIDC Libraries
In modern React applications, OAuth 2.0 protocols and PKCE mathematics are handled by certified OpenID Foundation libraries:

```bash
cd frontend
npm install react-oidc-context oidc-client-ts
```

* **`oidc-client-ts`:** The underlying TypeScript cryptographic engine that generates `code_verifier`, computes `code_challenge` (SHA-256), handles redirects, and securely stores JWTs.
* **`react-oidc-context`:** The React adapter providing the global Context Provider (`<AuthProvider>`) and custom hooks (`useAuth()`).

---

### Step 1.2: Cognito Configuration File (`src/auth/authConfig.ts`)
This file aggregates the 4 parameters from the AWS Console. It decouples identity infrastructure from UI components:

```typescript
// File: frontend/src/auth/authConfig.ts
import type { AuthProviderProps } from 'react-oidc-context';

// 1. Domain created under "App integration -> Domain" in the Cognito Console
const cognitoDomain = 'https://us-east-1ol9djb9xl.auth.us-east-1.amazoncognito.com';

export const cognitoAuthConfig: AuthProviderProps = {
  // 2. Authority: Unique User Pool URL (Issuer). AWS publishes OIDC discovery metadata here
  authority: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',

  // 3. Client ID: Public application identifier (created WITHOUT a client secret)
  client_id: '53acsgrc0tneq7jhigesj93g8r',

  // 4. Redirect URI: Where AWS redirects the browser after successful login
  // Using window.location.origin allows it to automatically work on localhost:5173 and Vercel
  redirect_uri: typeof window !== 'undefined' ? window.location.origin + '/' : 'https://soluciones-cloud-v1.vercel.app/',

  // 5. Response Type: 'code' forces the secure Authorization Code Flow with PKCE
  response_type: 'code',

  // 6. Scopes: User attributes requested from Cognito
  scope: 'email openid phone',

  // 7. Metadata: Exact OAuth2/OIDC endpoints provided by Cognito
  metadata: {
    issuer: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL',
    authorization_endpoint: `${cognitoDomain}/login`,
    token_endpoint: `${cognitoDomain}/oauth2/token`,
    userinfo_endpoint: `${cognitoDomain}/oauth2/userInfo`,
    end_session_endpoint: `${cognitoDomain}/logout`,
    jwks_uri: 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL/.well-known/jwks.json',
  },

  // 8. Callback to clean the URL query string once '?code=...' has been consumed
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};
```

---

### Step 1.3: Wrap the Application (`src/main.tsx`)
Wrap the root React tree with the provider to expose session state to all components:

```tsx
// File: frontend/src/main.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider } from 'react-oidc-context';
import { cognitoAuthConfig } from './auth/authConfig';
import { App } from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    {/* Injects Cognito session and state into the entire application */}
    <AuthProvider {...cognitoAuthConfig}>
      <App />
    </AuthProvider>
  </React.StrictMode>
);
```

---

### Step 1.4: Sign In and Sign Out UI (`src/components/Navbar.tsx`)
Consume the `useAuth()` hook to check session status and trigger redirects:

```tsx
// File: frontend/src/components/Navbar.tsx
import { useAuth } from 'react-oidc-context';

export const Navbar = () => {
  const auth = useAuth();

  if (auth.isLoading) {
    return <span>Checking authentication...</span>;
  }

  if (auth.isAuthenticated) {
    return (
      <div>
        {/* Cognito delivers user claims in auth.user.profile */}
        <span>Welcome, {auth.user?.profile.email}</span>
        <button onClick={() => auth.removeUser()}>Sign Out</button>
      </div>
    );
  }

  // Redirects user to the AWS Cognito Hosted UI with PKCE challenge
  return (
    <button onClick={() => auth.signinRedirect()}>
      Sign In with Cognito
    </button>
  );
};
```

---

### Step 1.5: Extract JWT and Attach to API Requests (`src/services/productService.ts`)
For protected operations (`POST`, `DELETE`), extract the token and attach it as a Bearer token:

```typescript
// File: frontend/src/services/productService.ts
export const productService = {
  async create(product: Product, token?: string): Promise<Product> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    // Attach JWT passport if user is authenticated
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch('https://8086dx45a7.execute-api.us-east-1.amazonaws.com/prod/api/products', {
      method: 'POST',
      headers,
      body: JSON.stringify(product),
    });

    if (response.status === 401) {
      throw new Error('Unauthorized: Invalid token or expired session.');
    }

    return response.json();
  }
};
```

---

# ☕ PHASE 2: BACKEND IMPLEMENTATION (SPRING BOOT / JAVA 21)

In this architecture, Spring Boot operates as a **Stateless OAuth2 Resource Server**. The backend does **not connect to Cognito's user database** or verify passwords; it only validates the cryptographic signature of incoming tokens.

---

### Step 2.1: Add Official Maven Dependency (`pom.xml`)
Add the official Spring Boot starter for Resource Servers:

```xml
<!-- File: demo/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

This starter includes:
1. `NimbusJwtDecoder` for asynchronous JWT verification.
2. Support for asymmetric cryptographic algorithms (**RS256**).
3. Automatic discovery of public keys via OpenID Connect.
4. The `BearerTokenAuthenticationFilter` security filter.

---

### Step 2.2: Configure Issuer URI (`application.properties`)
Spring Boot requires only a single property pointing to the Cognito User Pool:

```properties
# File: demo/src/main/resources/application.properties

# Issuer URI: Unique AWS Cognito User Pool endpoint
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OL9DjB9XL
```

#### What does Spring Boot do behind the scenes?
1. On boot, Spring Boot appends `/.well-known/openid-configuration` to the issuer URI.
2. It fetches the OpenID JSON document from AWS, discovering the JWKS endpoint (`/.well-known/jwks.json`).
3. It downloads the public keys and caches them in memory.
4. It is now ready to validate tokens locally without querying AWS on every incoming request.

---

### Step 2.3: Security Filter Chain (`SecurityConfig.java`)
Define route-level authorization rules:

```java
// File: demo/src/main/java/com/example/demo/config/SecurityConfig.java
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
            // 1. Enable CORS for Vercel and API Gateway calls
            .cors(Customizer.withDefaults())

            // 2. Disable CSRF (not needed for stateless REST APIs without session cookies)
            .csrf(AbstractHttpConfigurer::disable)

            // 3. Stateless session policy: No server-side HTTP session storage
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Route Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Allow browser preflight OPTIONS requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // GET /api/products is PUBLIC: anyone can read the catalog
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // POST and DELETE require a valid Cognito JWT
                .requestMatchers(HttpMethod.POST, "/api/products/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

                // Any other route requires authentication
                .anyRequest().authenticated()
            )

            // 5. ENABLE NATIVE OAUTH2 RESOURCE SERVER JWT VALIDATION
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
```

---

# 🔬 PHASE 3: INTERNAL REQUEST LIFECYCLE (WHAT HAPPENS IN CODE)

The exact execution sequence when a protected request reaches the backend:

```
[ Incoming HTTP Request ] ➔ [ 1. BearerTokenAuthenticationFilter ]
                                       │
                                       ▼
                            [ 2. NimbusJwtDecoder ] ◄── Uses Cached AWS JWKS
                                       │
                                       ├─► Valid RS256 signature against public key? (Yes/No)
                                       ├─► iss claim == User Pool Issuer URI? (Yes/No)
                                       └─► exp claim > Current Timestamp? (Yes/No)
                                       │
                        ┌──────────────┴──────────────┐
                     [ Valid ]                   [ Invalid ]
                        │                              │
                        ▼                              ▼
            [ SecurityContextHolder ]         [ HTTP 401 Unauthorized ]
             (Authenticated Subject)           (Immediate Edge Block)
                        │
                        ▼
              [ ProductController ]
             (Executes createProduct)
```

1. **Header Extraction:** Spring Security intercepts `Authorization: Bearer eyJhbGci...`.
2. **Key ID Matching:** Spring reads the `kid` claim in the JWT Header to match the corresponding public key from the cached JWKS.
3. **Cryptographic Signature Verification:** It applies the **RS256** asymmetric algorithm. If a bad actor changed `"role": "USER"` to `"role": "ADMIN"` in the Payload, the hash breaks and Spring throws a `BadJwtException`.
4. **Time & Issuer Validation:** It confirms that `exp` is in the future and `iss` matches our User Pool.
5. **Context Injection:** Spring instantiates a `JwtAuthenticationToken` and sets it into `SecurityContextHolder`.
6. **Controller Invocation:** Execution reaches `ProductController.java`, persisting the entity into **Supabase PostgreSQL**.

---

# 🚀 PHASE 4: MANDATORY AWS COGNITO CONSOLE CONFIGURATIONS

If an engineer misses any of these settings in AWS, the code will fail with redirect or CORS errors:

1. **App Client Configuration:**
   * **Do NOT generate a Client Secret:** In an SPA (React), the code runs in the user's browser. It is impossible to securely hide a secret. Enabling a client secret will break OIDC authentication.
2. **Allowed Callback URLs:**
   * Must exactly match the deployment URLs: `https://soluciones-cloud-v1.vercel.app/` and `http://localhost:5173/`. Missing a trailing slash `/` results in an immediate `redirect_mismatch` error.
3. **Sign-up Experience:**
   * Ensure *“Allow Cognito to automatically send messages for verification and confirmation”* is checked. This ensures users receive the 6-digit code and transition to `CONFIRMED` upon code entry without manual admin approval.

---

# 📌 SENIOR ENGINEER DEFENSE SCRIPT (ENGLISH)

If asked:
> *"Explain technically how you implemented Cognito and what specific modifications were made to the backend and frontend code."*

**Recommended Oral Response:**
> *"In the **Frontend (React)**, we integrated the certified `react-oidc-context` library. We created an `authConfig.ts` file mapping our **User Pool ID**, **App Client ID**, and **Hosted UI Domain**. We enforced the **Authorization Code Flow with PKCE** for public clients. Upon successful authentication, React receives the JWTs and attaches the token to outgoing requests in the `Authorization: Bearer <token>` header.*
> 
> *In the **Backend (Spring Boot)**, we avoided manual filter implementations. We added `spring-boot-starter-oauth2-resource-server` and configured `spring.security.oauth2.resourceserver.jwt.issuer-uri` in `application.properties`. Spring Security automatically discovers Amazon's JWKS endpoint on boot, caches the public keys, and performs asymmetric **RS256** signature and claims verification on every incoming request in a completely **Stateless** manner before dispatching to our controllers and persisting to Supabase."*

package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
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
            // 1. Habilitar CORS explícito para el Frontend en localhost:5173
            .cors(Customizer.withDefaults())

            // 2. Desactivar CSRF (no se requiere para APIs REST sin cookies de sesión)
            .csrf(AbstractHttpConfigurer::disable)

            // 3. Establecer sesión Stateless (cada petición debe llevar su propio token)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Reglas de autorización de endpoints
            .authorizeHttpRequests(auth -> auth
                // Permitir todas las peticiones preflight OPTIONS de CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Consola H2 para depuración
                .requestMatchers("/h2-console/**").permitAll()

                // GET /api/products es público (cualquiera puede ver el catálogo)
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // POST y DELETE requieren autenticación obligatoria con JWT
                .requestMatchers(HttpMethod.POST, "/api/products/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

                // Cualquier otra petición debe estar autenticada
                .anyRequest().authenticated()
            )

            // 5. Autenticación JWT nativa mediante OAuth2 Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))

            // 6. Permitir ver la consola H2 en frames
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

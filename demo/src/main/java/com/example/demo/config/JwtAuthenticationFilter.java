package com.example.demo.config;

import com.example.demo.service.JwtValidationService;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidationService jwtValidationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer la cabecera "Authorization"
        String authHeader = request.getHeader("Authorization");

        // 2. Si no viene cabecera o no empieza con "Bearer ", dejamos que la cadena continúe
        // (Spring Security decidirá si el endpoint requería autenticación o si era público)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Obtener el token sin la palabra "Bearer "
        String token = authHeader.substring(7);

        try {
            // 4. Validar el token con nuestro servicio criptográfico
            JWTClaimsSet claims = jwtValidationService.validateToken(token);

            // 5. Extraer el identificador del usuario (sub o username)
            String username = claims.getSubject();
            if (username == null) {
                username = claims.getStringClaim("username");
            }

            // 6. Si es válido, registrar al usuario en el contexto de seguridad de Spring
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Si el token es falso, expirado o manipulado, limpiar contexto de seguridad
            System.err.println("❌ Error validando token JWT: " + e.getMessage());
            e.printStackTrace();
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token JWT inválido: " + e.getMessage() + "\"}");
            return;
        }

        // 7. Continuar con la petición hacia el controlador
        filterChain.doFilter(request, response);
    }
}

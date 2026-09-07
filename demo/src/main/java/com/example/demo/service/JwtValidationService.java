package com.example.demo.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

@Service
public class JwtValidationService {

    @Value("${cognito.jwks-url}")
    private String jwksUrl;

    @Value("${cognito.issuer}")
    private String expectedIssuer;

    @Value("${cognito.client-id}")
    private String expectedClientId;

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @PostConstruct
    public void init() throws MalformedURLException {
        // 1. Configurar la fuente de claves públicas (JWKS de AWS Cognito)
        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUrl));

        // 2. Selector de clave esperando algoritmo asimétrico RS256 (estándar de Cognito)
        JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);

        jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);
    }

    /**
     * Valida el token JWT:
     * - Verifica la firma criptográfica contra el JWKS de AWS
     * - Comprueba que no esté expirado
     * - Comprueba que el emisor (iss) sea el de nuestro User Pool
     */
    public JWTClaimsSet validateToken(String token) throws Exception {
        // Procesa y verifica criptográficamente la firma con la clave pública de AWS
        JWTClaimsSet claimsSet = jwtProcessor.process(token, null);

        // Validar expiración
        Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime != null && new Date().after(expirationTime)) {
            throw new IllegalArgumentException("El token JWT ha expirado");
        }

        // Validar emisor (iss)
        String issuer = claimsSet.getIssuer();
        if (issuer == null || !issuer.equals(expectedIssuer)) {
            throw new IllegalArgumentException("El emisor del token es inválido: " + issuer);
        }

        return claimsSet;
    }
}

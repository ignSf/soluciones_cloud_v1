package com.example.demo;

import com.example.demo.config.CognitoAccessTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CognitoAccessTokenValidatorTest {

    private static final String EXPECTED_CLIENT_ID = "53acsgrc0tneq7jhigesj93g8r";
    private CognitoAccessTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CognitoAccessTokenValidator(EXPECTED_CLIENT_ID);
    }

    @Test
    @DisplayName("Debe aceptar un token válido con token_use=access y client_id correcto")
    void shouldAcceptValidAccessToken() {
        Jwt jwt = createJwt(Map.of(
                "token_use", "access",
                "client_id", EXPECTED_CLIENT_ID
        ));

        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertFalse(result.hasErrors(), "No debería tener errores de validación");
    }

    @Test
    @DisplayName("Debe rechazar un token si token_use es 'id' (id_token en lugar de access_token)")
    void shouldRejectIdToken() {
        Jwt jwt = createJwt(Map.of(
                "token_use", "id",
                "client_id", EXPECTED_CLIENT_ID
        ));

        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertTrue(result.hasErrors(), "Debe fallar si token_use no es 'access'");
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().iterator().next().getDescription().contains("token debe ser de tipo 'access'"));
    }

    @Test
    @DisplayName("Debe rechazar un token si pertenece a otro client_id no autorizado")
    void shouldRejectWrongClientId() {
        Jwt jwt = createJwt(Map.of(
                "token_use", "access",
                "client_id", "otro_client_id_desconocido"
        ));

        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertTrue(result.hasErrors(), "Debe fallar si client_id no coincide");
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().iterator().next().getDescription().contains("client_id del token no coincide"));
    }

    private Jwt createJwt(Map<String, Object> claims) {
        return new Jwt(
                "dummy-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                claims
        );
    }
}

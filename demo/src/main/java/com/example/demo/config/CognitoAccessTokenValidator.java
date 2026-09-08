package com.example.demo.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;

/**
 * Validador personalizado para tokens JWT emitidos por AWS Cognito.
 * Garantiza que:
 * 1. El token sea de tipo 'access' (rechazando 'id_token').
 * 2. El token pertenezca al 'client_id' de esta aplicación.
 */
public class CognitoAccessTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final String expectedClientId;

    public CognitoAccessTokenValidator(String expectedClientId) {
        this.expectedClientId = Objects.requireNonNull(expectedClientId, "expectedClientId no debe ser nulo");
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // 1. Validar token_use == "access"
        String tokenUse = jwt.getClaimAsString("token_use");
        if (!"access".equalsIgnoreCase(tokenUse)) {
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "El token debe ser de tipo 'access'. Se recibió: " + tokenUse,
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }

        // 2. Validar client_id esperado
        String clientId = jwt.getClaimAsString("client_id");
        if (!expectedClientId.equals(clientId)) {
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "El client_id del token no coincide con el autorizado para esta API",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}

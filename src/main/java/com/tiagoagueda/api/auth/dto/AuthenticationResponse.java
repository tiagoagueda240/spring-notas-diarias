package com.tiagoagueda.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta de autenticação contendo o access token JWT (curto prazo) e o refresh token opaco (longo prazo).
 */
public record AuthenticationResponse(
        @Schema(description = "Access token JWT válido por 15 minutos", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh token opaco (UUID) válido por 7 dias para renovar o access token")
        String refreshToken,

        @Schema(description = "Tipo de token", example = "Bearer")
        String tokenType,

        @Schema(description = "Role do utilizador autenticado", example = "USER")
        String role
) {
    public AuthenticationResponse(String accessToken, String refreshToken, String role) {
        this(accessToken, refreshToken, "Bearer", role);
    }
}

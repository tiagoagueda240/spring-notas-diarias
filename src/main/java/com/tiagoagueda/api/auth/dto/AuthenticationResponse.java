package com.tiagoagueda.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta de autenticação contendo o JWT.
 */
public record AuthenticationResponse(
        @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token
) {}
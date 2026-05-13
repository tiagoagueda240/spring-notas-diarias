package com.tiagoagueda.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "O refresh token é obrigatório")
        @Schema(description = "Refresh token opaco para obter novo access token")
        String refreshToken
) {}

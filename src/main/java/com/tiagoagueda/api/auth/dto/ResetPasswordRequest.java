package com.tiagoagueda.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "O token é obrigatório")
        @Schema(description = "Token de reset recebido no link de email")
        String token,

        @NotBlank
        @Size(min = 6, message = "A nova password tem de ter pelo menos 6 caracteres")
        @Schema(description = "Nova password para a conta", example = "novaPwd123")
        String newPassword
) {}

package com.tiagoagueda.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank @Email(message = "Email inválido")
        @Schema(description = "Email da conta para recuperação de password", example = "utilizador@email.com")
        String email
) {}

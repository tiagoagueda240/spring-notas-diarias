package com.tiagoagueda.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Pedido de autenticação com email e password.
 */
public record AuthenticationRequest(
        @Schema(description = "Email do utilizador", example = "nome@email.com")
        @NotBlank
        @Email(message = "Email inválido")
        String email,

        @Schema(description = "Password do utilizador", example = "123456")
        @NotBlank
        String password
) {}
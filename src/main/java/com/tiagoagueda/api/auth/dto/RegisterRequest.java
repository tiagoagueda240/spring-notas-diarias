package com.tiagoagueda.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Pedido de registo de novo utilizador.
 */
public record RegisterRequest(
        @Schema(description = "Nome do utilizador", example = "Nome do utilizador", required = true)
        @NotBlank String name,

        @Schema(description = "Email único do utilizador", example = "nome@email.com", required = true)
        @NotBlank @Email(message = "Email inválido") String email,

        @Schema(description = "Password com mínimo de 6 caracteres", example = "123456")
        @NotBlank @Size(min = 6, message = "A password tem de ter pelo menos 6 caracteres") String password
) {}
package com.tiagoagueda.api.core.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO padrão de erro devolvido pela API.
 *
 * É usado pelo GlobalExceptionHandler para respostas de erro consistentes.
 */
public record ErrorResponse(
        @Schema(description = "Código HTTP do erro", example = "400")
        int status,

        @Schema(description = "Mensagem descritiva do erro", example = "Email inválido")
        String message,

        @Schema(description = "Timestamp da ocorrência do erro", example = "2026-03-19T10:15:30")
        LocalDateTime timestamp
) {}
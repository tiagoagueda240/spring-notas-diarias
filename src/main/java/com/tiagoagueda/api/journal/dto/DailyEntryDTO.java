package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta de uma entrada de diário.
 */
public record DailyEntryDTO(
        @Schema(description = "Identificador da entrada", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Data da entrada", example = "2026-03-19")
        LocalDate entryDate,

        @Schema(description = "Texto original enviado pelo utilizador")
        String rawText,

        @Schema(description = "Indica se a IA já processou a entrada", example = "true")
        boolean aiProcessed,

        @Schema(description = "Lista de tarefas extraídas da entrada")
        List<TaskLogDTO> tasks
) {}
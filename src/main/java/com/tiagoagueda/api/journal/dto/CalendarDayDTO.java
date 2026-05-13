package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Ponto de dados para o heatmap de calendário.
 * Um registo por dia com entradas.
 */
public record CalendarDayDTO(

        @Schema(description = "Data do registo", example = "2026-03-19")
        LocalDate date,

        @Schema(description = "Score médio de impacto das tarefas desse dia (0.0 se sem tarefas)", example = "3.75")
        double avgScore,

        @Schema(description = "Número de entradas nesse dia", example = "1")
        int entryCount,

        @Schema(description = "Humor/energia registado (1-5), null se não preenchido", nullable = true)
        Integer mood
) {}

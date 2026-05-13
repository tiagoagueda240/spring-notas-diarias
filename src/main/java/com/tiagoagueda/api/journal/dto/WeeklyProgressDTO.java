package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Breakdown de produtividade para uma semana")
public record WeeklyProgressDTO(
        @Schema(description = "Data de início da semana") LocalDate weekStart,
        @Schema(description = "Score médio de impacto nessa semana (0-5)") double averageScore,
        @Schema(description = "Número de tarefas nessa semana") int taskCount
) {}

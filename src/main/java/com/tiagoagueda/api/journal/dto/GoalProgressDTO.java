package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Métricas de progresso face ao objetivo definido no onboarding")
public record GoalProgressDTO(
        @Schema(description = "Texto do objetivo definido") String goal,
        @Schema(description = "Data em que o objetivo foi definido") LocalDate goalSetAt,
        @Schema(description = "Percentagem de progresso (0-100) baseada no score médio") double progressPercentage,
        @Schema(description = "Score médio de impacto desde que o objetivo foi definido") double averageImpactScore,
        @Schema(description = "Dias com entradas registadas desde o início do objetivo") int daysTracked,
        @Schema(description = "Tendência face às últimas semanas: IMPROVING, STABLE ou DECLINING") String trend,
        @Schema(description = "Breakdown semanal das últimas 12 semanas") List<WeeklyProgressDTO> weeklyBreakdown
) {}

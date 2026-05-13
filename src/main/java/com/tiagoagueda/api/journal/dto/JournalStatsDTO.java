package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estatísticas calculadas no servidor para o diário do utilizador")
public record JournalStatsDTO(
        @Schema(description = "Sequência de dias consecutivos com entradas até hoje") int currentStreak,
        @Schema(description = "Maior sequência consecutiva de sempre") int longestStreak,
        @Schema(description = "Score médio de impacto de todas as tarefas (0-5)") double averageImpactScore,
        @Schema(description = "Número de tarefas com score de impacto >= 4") long highImpactTasksCount,
        @Schema(description = "Total de entradas no diário") long totalEntries,
        @Schema(description = "Total de tarefas registadas") long totalTasks
) {}

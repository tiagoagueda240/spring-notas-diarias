package com.tiagoagueda.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Badge/conquista desbloqueada ou não pelo utilizador.
 */
public record AchievementDTO(

        @Schema(description = "Identificador único da conquista", example = "STREAK_7")
        String id,

        @Schema(description = "Título da conquista", example = "Semana Perfeita")
        String title,

        @Schema(description = "Descrição do critério", example = "7 dias consecutivos de registo")
        String description,

        @Schema(description = "Indica se a conquista está desbloqueada")
        boolean unlocked,

        @Schema(description = "Progresso atual face ao objetivo (ex: 4 de 7 dias)", example = "4")
        long currentProgress,

        @Schema(description = "Objetivo necessário para desbloquear", example = "7")
        long targetProgress
) {}

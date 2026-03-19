package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta para uma tarefa extraída da entrada diária.
 */
public record TaskLogDTO(
        @Schema(description = "Identificador da tarefa", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID id,

        @Schema(description = "Título curto da tarefa", example = "Correção de bug no login")
        String title,

        @Schema(description = "Descrição profissional da tarefa")
        String description,

        @Schema(description = "Impacto no negócio de 1 a 5", example = "4")
        int impactScore,

        @Schema(description = "Justificação resumida do impacto")
        String impactJustification,

        @Schema(description = "Tags atribuídas à tarefa")
        List<String> tags
) {}
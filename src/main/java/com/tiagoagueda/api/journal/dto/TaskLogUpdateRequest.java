package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TaskLogUpdateRequest(
        @NotBlank(message = "O título não pode estar vazio")
        @Schema(description = "Título da tarefa", example = "Otimização DB")
        String title,

        @NotBlank(message = "A descrição não pode estar vazia")
        @Schema(description = "Descrição profissional da tarefa")
        String description,

        @Min(value = 1, message = "O impacto mínimo é 1")
        @Max(value = 5, message = "O impacto máximo é 5")
        @Schema(description = "Novo score de impacto", example = "5")
        int impactScore,

        @NotBlank(message = "A justificação não pode estar vazia")
        @Schema(description = "Justificação resumida do impacto")
        String impactJustification,

        @Schema(description = "Lista atualizada de tags")
        List<String> tags
) {}
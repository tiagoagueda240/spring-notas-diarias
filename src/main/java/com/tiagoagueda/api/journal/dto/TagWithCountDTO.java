package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tag com contagem de uso nas tarefas do utilizador")
public record TagWithCountDTO(
        @Schema(description = "Nome da tag") String name,
        @Schema(description = "Número de tarefas em que esta tag foi usada") long count
) {}

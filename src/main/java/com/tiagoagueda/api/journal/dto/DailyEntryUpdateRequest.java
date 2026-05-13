package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DailyEntryUpdateRequest(
        @NotBlank(message = "O texto atualizado não pode estar vazio")
        @Schema(description = "Texto atualizado do diário")
        String text
) {}
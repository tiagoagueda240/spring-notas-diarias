package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DailyEntryUpdateRequest(
        @NotBlank(message = "O texto atualizado não pode estar vazio")
        @Schema(description = "Texto atualizado do diário")
        String text,

        @Min(value = 1, message = "O humor deve ser pelo menos 1")
        @Max(value = 5, message = "O humor não pode ser superior a 5")
        @Schema(description = "Energia/humor do dia (1=muito mau, 5=excelente)", example = "4", nullable = true)
        Integer mood
) {}
package com.tiagoagueda.api.journal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * DTO de pedido para criação de uma entrada de diário.
 */
public record DailyEntryRequest(
        @NotBlank(message = "O relato diário não pode estar vazio")
        @Schema(description = "Texto livre do diário", example = "Hoje corrigi um bug crítico na API e validei o deploy.")
        String text,

        @Min(value = 1, message = "O humor deve ser pelo menos 1")
        @Max(value = 5, message = "O humor não pode ser superior a 5")
        @Schema(description = "Energia/humor do dia (1=muito mau, 5=excelente)", example = "4", nullable = true)
        Integer mood,

        @PastOrPresent(message = "A data não pode ser no futuro")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "Data da entrada (omitir para usar o dia de hoje)", example = "2026-05-13", nullable = true)
        LocalDate date
) {}

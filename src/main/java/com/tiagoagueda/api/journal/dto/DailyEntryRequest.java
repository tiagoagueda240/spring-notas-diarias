package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de pedido para criação de uma entrada de diário.
 */
public record DailyEntryRequest(@NotBlank(message = "O relato diário não pode estar vazio")
                                @Schema(description = "Texto livre do diário", example = "Hoje corrigi um bug crítico na API e validei o deploy.")
                                String text) {
}

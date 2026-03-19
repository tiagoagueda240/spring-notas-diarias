package com.tiagoagueda.api.journal.dto;

import jakarta.validation.constraints.NotBlank;

// Um Record é uma classe imutável perfeita para receber dados de fora.
// O Angular só nos vai enviar um JSON assim: { "text": "Hoje fiz..." }
public record DailyEntryRequest(@NotBlank(message = "O relato diário não pode estar vazio")
                                String text) {
}

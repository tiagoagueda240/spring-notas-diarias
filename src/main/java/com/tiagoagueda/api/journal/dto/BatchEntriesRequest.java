package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record BatchEntriesRequest(
        @NotEmpty(message = "A lista de datas não pode estar vazia")
        @Size(max = 31, message = "Máximo de 31 datas por pedido batch")
        @Schema(description = "Lista de datas a obter (máximo 31)", example = "[\"2025-01-01\",\"2025-01-02\"]")
        List<LocalDate> dates
) {}

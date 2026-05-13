package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Entrada de diário para um dia específico no resultado batch")
public record BatchEntryDTO(
        @Schema(description = "A data solicitada") LocalDate date,
        @Schema(description = "A entrada de diário para esse dia, ou null se não existir") DailyEntryDTO entry
) {}

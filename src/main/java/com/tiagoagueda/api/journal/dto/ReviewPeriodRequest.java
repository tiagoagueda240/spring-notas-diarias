package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReviewPeriodRequest(
        @NotNull(message = "O período é obrigatório")
        @Schema(description = "Período para geração do relatório IA: WEEK (7 dias), MONTH (30 dias), QUARTER (90 dias)",
                example = "MONTH")
        ReviewPeriod period
) {
    public enum ReviewPeriod {
        WEEK, MONTH, QUARTER
    }
}

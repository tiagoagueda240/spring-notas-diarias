package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PerformanceReviewRequest(
        @NotNull(message = "A data de início é obrigatória")
        @Schema(description = "Data de início da avaliação", example = "2025-01-01")
        LocalDate startDate,

        @NotNull(message = "A data de fim é obrigatória")
        @Schema(description = "Data de fim da avaliação", example = "2025-12-31")
        LocalDate endDate
) {}
package com.tiagoagueda.api.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PerformanceReviewResponse(
        @Schema(description = "Relatório formatado em Markdown gerado pela IA")
        String reportContent
) {}
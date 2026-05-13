package com.tiagoagueda.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGoalRequest(
        @NotBlank(message = "O objetivo não pode estar vazio")
        @Size(max = 500, message = "O objetivo não pode ter mais de 500 caracteres")
        @Schema(description = "Objetivo de carreira a atingir", example = "Tornar-me Tech Lead em 12 meses")
        String goal
) {}

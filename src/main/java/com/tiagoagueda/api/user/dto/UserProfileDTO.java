package com.tiagoagueda.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Perfil do utilizador autenticado")
public record UserProfileDTO(
        UUID id,
        String name,
        String email,
        @Schema(description = "Objetivo de carreira definido no onboarding") String goal,
        @Schema(description = "Data em que o objetivo foi definido") LocalDate goalSetAt
) {}

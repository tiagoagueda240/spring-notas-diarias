package com.tiagoagueda.api.admin.dto;

import com.tiagoagueda.api.user.Plan;
import com.tiagoagueda.api.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Visão administrativa de um utilizador")
public record AdminUserDTO(
        UUID id,
        String name,
        String email,
        Role role,
        Plan plan,
        LocalDate goalSetAt,
        long totalEntries
) {}

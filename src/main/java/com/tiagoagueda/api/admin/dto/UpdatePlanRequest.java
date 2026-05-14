package com.tiagoagueda.api.admin.dto;

import com.tiagoagueda.api.user.Plan;
import jakarta.validation.constraints.NotNull;

public record UpdatePlanRequest(
        @NotNull Plan plan
) {}

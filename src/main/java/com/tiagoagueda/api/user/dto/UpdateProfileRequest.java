package com.tiagoagueda.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 200, message = "O nome não pode ter mais de 200 caracteres")
        @Schema(description = "Nome do utilizador", example = "Ana Silva")
        String name,

        @Size(max = 200, message = "A profissão não pode ter mais de 200 caracteres")
        @Schema(description = "Profissão do utilizador — usada pela IA para contextualizar a análise das tarefas",
                example = "Educadora de Infância")
        String profession
) {}

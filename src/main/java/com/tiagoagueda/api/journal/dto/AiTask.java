package com.tiagoagueda.api.journal.dto;

import java.util.List;

// Isto representa uma única tarefa que a IA encontrou no teu texto
public record AiTask(
        String title,
        String description,
        int impactScore,
        String impactJustification,
        List<String> tags
) {}
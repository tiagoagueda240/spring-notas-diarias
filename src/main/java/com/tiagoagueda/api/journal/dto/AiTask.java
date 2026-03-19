package com.tiagoagueda.api.journal.dto;

import java.util.List;

/**
 * Representa uma tarefa extraída pela IA a partir do texto do diário.
 */
public record AiTask(
        String title,
        String description,
        int impactScore,
        String impactJustification,
        List<String> tags
) {}
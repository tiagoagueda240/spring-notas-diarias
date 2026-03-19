package com.tiagoagueda.api.journal.dto;

import java.util.List;
import java.util.UUID;

public record TaskLogDTO(
        UUID id,
        String title,
        String description,
        int impactScore,
        String impactJustification,
        List<String> tags
) {}
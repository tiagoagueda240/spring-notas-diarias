package com.tiagoagueda.api.journal.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DailyEntryDTO(
        UUID id,
        LocalDate entryDate,
        String rawText,
        boolean aiProcessed,
        List<TaskLogDTO> tasks
) {}
package com.tiagoagueda.api.journal.dto;

import java.util.List;

// Como num dia podes fazer várias tarefas, pedimos à IA uma lista delas
public record AiExtractedDailyLog(
        List<AiTask> tasks
) {}

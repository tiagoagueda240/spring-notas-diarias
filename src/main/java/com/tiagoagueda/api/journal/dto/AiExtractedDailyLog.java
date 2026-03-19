package com.tiagoagueda.api.journal.dto;

import java.util.List;

/**
 * Estrutura de resposta esperada da IA contendo as tarefas identificadas no dia.
 */
public record AiExtractedDailyLog(
        List<AiTask> tasks
) {}

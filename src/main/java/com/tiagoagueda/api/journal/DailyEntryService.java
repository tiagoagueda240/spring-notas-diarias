package com.tiagoagueda.api.journal;

import com.tiagoagueda.api.core.exception.DuplicateEntryException;
import com.tiagoagueda.api.journal.dto.*;
import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.journal.entity.DailyEntry;
import com.tiagoagueda.api.journal.entity.Tag;
import com.tiagoagueda.api.journal.entity.TaskLog;
import com.tiagoagueda.api.journal.repository.DailyEntryRepository;
import com.tiagoagueda.api.journal.repository.DailyEntrySpecification;
import com.tiagoagueda.api.journal.repository.TagRepository;
import com.tiagoagueda.api.journal.repository.TaskLogRepository;
import com.tiagoagueda.api.user.dto.AchievementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DailyEntryService {

    private final DailyEntryRepository dailyEntryRepository;
    private final TaskLogRepository taskLogRepository;
    private final TagRepository tagRepository;
    private final ChatClient chatClient;
    private final TransactionTemplate transactionTemplate;

    private static final Logger log = LoggerFactory.getLogger(DailyEntryService.class);

    public DailyEntryService(DailyEntryRepository dailyEntryRepository,
                             TaskLogRepository taskLogRepository,
                             TagRepository tagRepository,
                             ChatClient.Builder chatClientBuilder,
                             PlatformTransactionManager transactionManager) {
        this.dailyEntryRepository = dailyEntryRepository;
        this.taskLogRepository = taskLogRepository;
        this.tagRepository = tagRepository;
        this.chatClient = chatClientBuilder.build();
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    // -------------------------------------------------------------------------
    // CRUD de entradas
    // -------------------------------------------------------------------------

    public DailyEntryDTO saveEntry(String rawText, Integer mood, LocalDate date, AppUser currentUser) {
        log.info("A criar nova entrada de diário para o utilizador: {}", currentUser.getEmail());

        LocalDate entryDate = (date != null) ? date : LocalDate.now();
        if (dailyEntryRepository.findByAppUserAndEntryDate(currentUser, entryDate).isPresent()) {
            throw new DuplicateEntryException(
                    "Já existe uma entrada de diário para " + entryDate + ". Usa o endpoint de edição para a atualizar.");
        }

        DailyEntry newEntry = DailyEntry.builder()
                .entryDate(entryDate)
                .rawText(rawText)
                .mood(mood)
                .build();
        newEntry.setAppUser(currentUser);

        DailyEntry savedEntry = dailyEntryRepository.save(newEntry);
        populateEntryWithAiTasks(savedEntry, rawText, currentUser);
        return convertToDTO(savedEntry);
    }

    public DailyEntryDTO updateEntry(UUID id, String newText, Integer mood, AppUser currentUser) {
        log.info("A atualizar entrada {} do utilizador {}", id, currentUser.getEmail());

        DailyEntry entry = dailyEntryRepository.findByIdAndAppUser(id, currentUser)
                .orElseThrow(() -> new NoSuchElementException("Diário não encontrado ou sem permissão."));

        entry.setRawText(newText);
        if (mood != null) entry.setMood(mood);
        entry.setAiProcessed(false);
        taskLogRepository.deleteAll(entry.getTasks());
        entry.getTasks().clear();

        DailyEntry updated = dailyEntryRepository.save(entry);
        populateEntryWithAiTasks(updated, newText, currentUser);
        return convertToDTO(updated);
    }

    /**
     * Reprocessa a entrada existente com IA sem alterar o texto original.
     * Ãštil quando o modelo melhora ou o utilizador quer re-analisar.
     */
    public DailyEntryDTO reprocessEntry(UUID id, AppUser currentUser) {
        log.info("A reprocessar entrada {} do utilizador {}", id, currentUser.getEmail());

        DailyEntry entry = dailyEntryRepository.findByIdAndAppUser(id, currentUser)
                .orElseThrow(() -> new NoSuchElementException("DiÃ¡rio nÃ£o encontrado ou sem permissÃ£o."));

        taskLogRepository.deleteAll(entry.getTasks());
        entry.getTasks().clear();
        entry.setAiProcessed(false);
        dailyEntryRepository.save(entry);

        populateEntryWithAiTasks(entry, entry.getRawText(), currentUser);
        return convertToDTO(entry);
    }

    public Page<DailyEntryDTO> findAllEntries(Pageable pageable, AppUser currentUser) {
        return dailyEntryRepository.findByAppUserOrderByEntryDateDesc(currentUser, pageable)
                .map(this::convertToDTO);
    }

    // -------------------------------------------------------------------------
    // Search & Filter
    // -------------------------------------------------------------------------

    public Page<DailyEntryDTO> searchEntries(
            AppUser currentUser,
            String q,
            List<String> tags,
            Integer minScore,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        var spec = DailyEntrySpecification.buildFilter(currentUser, q, tags, minScore, from, to);
        return dailyEntryRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    // -------------------------------------------------------------------------
    // Calendar Heatmap
    // -------------------------------------------------------------------------

    public List<CalendarDayDTO> getCalendarHeatmap(AppUser currentUser, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<Object[]> rows = dailyEntryRepository.findCalendarData(currentUser, start, end);
        return rows.stream().map(row -> new CalendarDayDTO(
                (LocalDate) row[0],
                row[1] instanceof Number n ? Math.round(n.doubleValue() * 100.0) / 100.0 : 0.0,
                row[2] instanceof Number n ? n.intValue() : 0,
                row[3] instanceof Number n ? n.intValue() : null
        )).toList();
    }

    public Optional<DailyEntryDTO> findByIdForUser(UUID id, AppUser currentUser) {
        return dailyEntryRepository.findByIdAndAppUser(id, currentUser).map(this::convertToDTO);
    }

    public void deleteEntry(UUID id, AppUser currentUser) {
        DailyEntry entry = dailyEntryRepository.findByIdAndAppUser(id, currentUser)
                .orElseThrow(() -> new NoSuchElementException("DiÃ¡rio nÃ£o encontrado ou nÃ£o tens permissÃ£o para o apagar."));
        dailyEntryRepository.delete(entry);
    }

    // -------------------------------------------------------------------------
    // CRUD de tarefas
    // -------------------------------------------------------------------------

    public TaskLogDTO updateTask(UUID taskId, TaskLogUpdateRequest request, AppUser currentUser) {
        log.info("O utilizador {} estÃ¡ a tentar editar a tarefa {}", currentUser.getEmail(), taskId);

        TaskLog task = taskLogRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Tarefa nÃ£o encontrada."));

        if (!task.getDailyEntry().getAppUser().getId().equals(currentUser.getId())) {
            throw new NoSuchElementException("Tarefa nÃ£o encontrada.");
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setImpactScore(request.impactScore());
        task.setImpactJustification(request.impactJustification());

        Set<Tag> updatedTags = new HashSet<>();
        if (request.tags() != null) {
            for (String tagName : request.tags()) {
                updatedTags.add(getOrCreateTag(tagName));
            }
        }
        task.setTags(updatedTags);
        taskLogRepository.save(task);
        return convertTaskToDTO(task);
    }

    public TaskLogDTO addTaskManually(UUID entryId, TaskLogUpdateRequest request, AppUser currentUser) {
        DailyEntry entry = dailyEntryRepository.findByIdAndAppUser(entryId, currentUser)
                .orElseThrow(() -> new NoSuchElementException("DiÃ¡rio nÃ£o encontrado ou sem permissÃ£o."));

        TaskLog taskLog = buildTaskLog(request.title(), request.description(),
                request.impactScore(), request.impactJustification(), request.tags());
        entry.addTask(taskLog);
        taskLogRepository.save(taskLog);
        return convertTaskToDTO(taskLog);
    }

    public void deleteTask(UUID taskId, AppUser currentUser) {
        TaskLog task = taskLogRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Tarefa nÃ£o encontrada."));

        if (!task.getDailyEntry().getAppUser().getId().equals(currentUser.getId())) {
            throw new NoSuchElementException("Tarefa nÃ£o encontrada.");
        }
        taskLogRepository.delete(task);
    }

    // -------------------------------------------------------------------------
    // Batch: obter mÃºltiplos dias numa sÃ³ chamada
    // -------------------------------------------------------------------------

    public List<BatchEntryDTO> getBatchEntries(List<LocalDate> dates, AppUser currentUser) {
        List<DailyEntry> found = dailyEntryRepository.findByAppUserAndEntryDateIn(currentUser, dates);
        // toMap with merge function handles duplicate entries per date (last wins)
        Map<LocalDate, DailyEntryDTO> byDate = found.stream()
                .collect(Collectors.toMap(
                        DailyEntry::getEntryDate,
                        this::convertToDTO,
                        (existing, replacement) -> replacement
                ));
        return dates.stream()
                .map(date -> new BatchEntryDTO(date, byDate.get(date)))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // EstatÃ­sticas calculadas no servidor
    // -------------------------------------------------------------------------

    public JournalStatsDTO getJournalStats(AppUser currentUser) {
        List<LocalDate> dates = dailyEntryRepository.findAllEntryDatesByUser(currentUser);
        int currentStreak = calculateCurrentStreak(dates);
        int longestStreak = calculateLongestStreak(dates);
        long totalEntries = dates.size();
        long totalTasks = taskLogRepository.countByUser(currentUser);
        double avgScore = taskLogRepository.avgImpactScoreByUser(currentUser);
        long highImpact = taskLogRepository.countHighImpactByUser(currentUser);
        return new JournalStatsDTO(currentStreak, longestStreak,
                Math.round(avgScore * 100.0) / 100.0, highImpact, totalEntries, totalTasks);
    }

    // -------------------------------------------------------------------------
    // Achievements / Badges
    // -------------------------------------------------------------------------

    public List<AchievementDTO> getAchievements(AppUser currentUser) {
        JournalStatsDTO stats = getJournalStats(currentUser);
        long totalTasks = stats.totalTasks();
        long totalEntries = stats.totalEntries();
        int currentStreak = stats.currentStreak();
        int longestStreak = stats.longestStreak();
        long highImpact = stats.highImpactTasksCount();

        List<AchievementDTO> achievements = new ArrayList<>();

        achievements.add(badge("FIRST_ENTRY", "Primeira Entrada",
                "Registaste o teu primeiro dia de trabalho",
                totalEntries >= 1, totalEntries, 1));

        achievements.add(badge("STREAK_3", "Hat-trick",
                "3 dias consecutivos de registo",
                currentStreak >= 3, currentStreak, 3));

        achievements.add(badge("STREAK_7", "Semana Perfeita",
                "7 dias consecutivos de registo",
                currentStreak >= 7 || longestStreak >= 7,
                Math.max(currentStreak, longestStreak), 7));

        achievements.add(badge("STREAK_30", "Mês Inabalável",
                "30 dias consecutivos de registo",
                longestStreak >= 30, longestStreak, 30));

        achievements.add(badge("FIRST_HIGH_IMPACT", "Alto Impacto",
                "Primeira tarefa com score 5/5",
                highImpact >= 1, Math.min(highImpact, 1), 1));

        achievements.add(badge("HIGH_IMPACT_10", "Máquina de Resultados",
                "10 tarefas com score de impacto ≥ 4",
                highImpact >= 10, highImpact, 10));

        achievements.add(badge("TASKS_50", "50 Conquistas",
                "Registaste 50 tarefas no total",
                totalTasks >= 50, totalTasks, 50));

        achievements.add(badge("TASKS_200", "Lenda da Produtividade",
                "200 tarefas registadas",
                totalTasks >= 200, totalTasks, 200));

        achievements.add(badge("ENTRIES_30", "Escritor Consistente",
                "30 dias de registo ao longo do tempo",
                totalEntries >= 30, totalEntries, 30));

        achievements.add(badge("ENTRIES_100", "Centúsimo Dia",
                "100 dias de registo ao longo do tempo",
                totalEntries >= 100, totalEntries, 100));

        return achievements;
    }

    private AchievementDTO badge(String id, String title, String description,
                                 boolean unlocked, long current, long target) {
        return new AchievementDTO(id, title, description, unlocked,
                Math.min(current, target), target);
    }

    // -------------------------------------------------------------------------
    // Tags com contagem
    // -------------------------------------------------------------------------

    public List<TagWithCountDTO> getTagsWithCount(AppUser currentUser) {
        return taskLogRepository.findTagsWithCountByUser(currentUser).stream()
                .map(row -> new TagWithCountDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // RelatÃ³rio IA: por perÃ­odo prÃ©-definido
    // -------------------------------------------------------------------------

    public PerformanceReviewResponse generateReviewByPeriod(AppUser currentUser, ReviewPeriodRequest.ReviewPeriod period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (period) {
            case WEEK -> endDate.minusDays(7);
            case MONTH -> endDate.minusDays(30);
            case QUARTER -> endDate.minusDays(90);
        };
        return generatePerformanceReview(currentUser, startDate, endDate);
    }

    public PerformanceReviewResponse generatePerformanceReview(AppUser currentUser, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de fim.");
        }
        List<DailyEntry> entries = dailyEntryRepository
                .findByAppUserAndEntryDateBetweenOrderByEntryDateAsc(currentUser, startDate, endDate);

        if (entries.isEmpty()) {
            return new PerformanceReviewResponse("NÃ£o existem registos suficientes neste perÃ­odo para gerar um relatÃ³rio.");
        }

        StringBuilder workData = new StringBuilder();
        entries.forEach(entry -> entry.getTasks().forEach(task -> workData.append(
                String.format("- [%s] %s (Impacto: %d/5) - JustificaÃ§Ã£o: %s\n",
                        entry.getEntryDate(), task.getTitle(), task.getImpactScore(), task.getImpactJustification())
        )));

        String prompt = """
                Atuas como um conselheiro de carreira executivo e especialista em recursos humanos.
                O teu cliente quer pedir um aumento salarial e forneceu-te o registo das suas tarefas e impacto nos Ãºltimos meses.
                
                Com base na lista de tarefas abaixo, escreve um relatÃ³rio de avaliaÃ§Ã£o de desempenho persuasivo, altamente profissional e bem estruturado.
                
                O documento deve conter:
                1. **Resumo Executivo**: Um parÃ¡grafo de impacto sobre o valor trazido Ã  empresa.
                2. **Principais Entregas e Impacto**: Agrupa as tarefas por temas e destaca as que tÃªm Score de Impacto 4 ou 5.
                3. **ConsistÃªncia e Fiabilidade**: Menciona a cadÃªncia de entregas.
                4. **Proposta de Valor Final**: Um argumento de fecho sÃ³lido justificando a progressÃ£o salarial ou de carreira.
                
                Usa formataÃ§Ã£o Markdown. Evita linguagem arrogante, mas sÃª extremamente confiante e orientado a dados.
                
                DADOS DE TRABALHO DO COLABORADOR:
                %s
                """.formatted(workData);

        String aiReport = chatClient.prompt().user(prompt).call().content();
        return new PerformanceReviewResponse(aiReport);
    }

    // -------------------------------------------------------------------------
    // Exportar Brag Document (Markdown)
    // -------------------------------------------------------------------------

    public String generateBragDocument(AppUser currentUser, LocalDate startDate, LocalDate endDate) {
        List<DailyEntry> entries = dailyEntryRepository
                .findByAppUserAndEntryDateBetweenOrderByEntryDateAsc(currentUser, startDate, endDate);

        long totalTasks = entries.stream().mapToLong(e -> e.getTasks().size()).sum();
        OptionalDouble avgScore = entries.stream()
                .flatMap(e -> e.getTasks().stream())
                .mapToInt(TaskLog::getImpactScore)
                .average();

        StringBuilder sb = new StringBuilder();
        sb.append("# Brag Document\n\n");
        sb.append("**Colaborador:** ").append(currentUser.getName()).append("\n");
        sb.append("**PerÃ­odo:** ").append(startDate).append(" a ").append(endDate).append("\n\n");
        sb.append("---\n\n");
        sb.append("## Resumo\n\n");
        sb.append("- Dias registados: **").append(entries.size()).append("**\n");
        sb.append("- Tarefas totais: **").append(totalTasks).append("**\n");
        avgScore.ifPresent(avg ->
                sb.append("- Score mÃ©dio de impacto: **").append(String.format("%.1f", avg)).append("/5**\n"));
        sb.append("\n---\n\n");

        sb.append("## Conquistas de Alto Impacto (Score â‰¥ 4)\n\n");
        boolean hasHighImpact = false;
        for (DailyEntry entry : entries) {
            for (TaskLog task : entry.getTasks()) {
                if (task.getImpactScore() >= 4) {
                    hasHighImpact = true;
                    sb.append("### ").append(entry.getEntryDate()).append(" â€” ").append(task.getTitle()).append("\n");
                    sb.append("> Score: **").append(task.getImpactScore()).append("/5** â€” ")
                            .append(task.getImpactJustification()).append("\n\n");
                    if (task.getDescription() != null) {
                        sb.append(task.getDescription()).append("\n\n");
                    }
                }
            }
        }
        if (!hasHighImpact) sb.append("_Nenhuma tarefa de alto impacto neste perÃ­odo._\n\n");

        sb.append("---\n\n");
        sb.append("## Todas as ContribuiÃ§Ãµes\n\n");
        entries.forEach(entry -> {
            if (!entry.getTasks().isEmpty()) {
                sb.append("### ").append(entry.getEntryDate()).append("\n\n");
                entry.getTasks().forEach(task -> {
                    sb.append("- **").append(task.getTitle())
                            .append("** _(Score: ").append(task.getImpactScore()).append("/5)_\n");
                    if (task.getDescription() != null) {
                        sb.append("  ").append(task.getDescription()).append("\n");
                    }
                });
                sb.append("\n");
            }
        });

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Progresso face ao objetivo
    // -------------------------------------------------------------------------

    public GoalProgressDTO calculateGoalProgress(AppUser currentUser) {
        if (currentUser.getGoal() == null || currentUser.getGoalSetAt() == null) {
            return new GoalProgressDTO(null, null, 0, 0, 0, "N/A", List.of());
        }

        LocalDate goalStart = currentUser.getGoalSetAt();
        LocalDate today = LocalDate.now();

        List<DailyEntry> entries = dailyEntryRepository
                .findByAppUserAndEntryDateBetweenOrderByEntryDateAsc(currentUser, goalStart, today);

        int daysTracked = entries.size();
        double avgScore = entries.stream()
                .flatMap(e -> e.getTasks().stream())
                .mapToInt(TaskLog::getImpactScore)
                .average()
                .orElse(0.0);

        double progressPct = Math.min((avgScore / 5.0) * 100, 100);
        List<WeeklyProgressDTO> weeklyBreakdown = calculateWeeklyBreakdown(entries, today);
        String trend = calculateTrend(weeklyBreakdown);

        return new GoalProgressDTO(
                currentUser.getGoal(),
                goalStart,
                Math.round(progressPct * 10.0) / 10.0,
                Math.round(avgScore * 100.0) / 100.0,
                daysTracked,
                trend,
                weeklyBreakdown
        );
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void populateEntryWithAiTasks(DailyEntry entry, String text, AppUser user) {
        try {
            log.info("A contactar IA para extrair tarefas...");
            List<DailyEntry> recentEntries = dailyEntryRepository
                    .findTop7ByAppUserAndAiProcessedTrueOrderByEntryDateDesc(user);
            AiExtractedDailyLog extractedData = extractTasksWithAI(text, user, recentEntries);
            if (extractedData != null && extractedData.tasks() != null) {
                transactionTemplate.executeWithoutResult(status -> {
                    for (AiTask aiTask : extractedData.tasks()) {
                        TaskLog taskLog = buildTaskLog(aiTask.title(), aiTask.description(),
                                aiTask.impactScore(), aiTask.impactJustification(), aiTask.tags());
                        entry.addTask(taskLog);
                        taskLogRepository.save(taskLog);
                    }
                    entry.setAiProcessed(true);
                    dailyEntryRepository.save(entry);
                });
            }
        } catch (Exception e) {
            log.error("Falha ao processar texto com a IA: {}", e.getMessage(), e);
        }
    }

    private TaskLog buildTaskLog(String title, String description, int impactScore,
                                 String impactJustification, List<String> tagNames) {
        TaskLog taskLog = new TaskLog();
        taskLog.setTitle(title);
        taskLog.setDescription(description);
        taskLog.setImpactScore(impactScore);
        taskLog.setImpactJustification(impactJustification);
        Set<Tag> tags = new HashSet<>();
        if (tagNames != null) {
            tagNames.forEach(name -> {
                Tag t = getOrCreateTag(name);
                if (t != null) tags.add(t);
            });
        }
        taskLog.setTags(tags);
        return taskLog;
    }

    private AiExtractedDailyLog extractTasksWithAI(String text, AppUser user, List<DailyEntry> recentEntries) {
        var converter = new BeanOutputConverter<>(AiExtractedDailyLog.class);
        String formatInstructions = converter.getFormat();

        String profession = (user.getProfession() != null && !user.getProfession().isBlank())
                ? user.getProfession() : "não especificada";
        String goal = (user.getGoal() != null && !user.getGoal().isBlank())
                ? user.getGoal() : "não definido";
        String name = (user.getName() != null && !user.getName().isBlank())
                ? user.getName() : "Utilizador";

        StringBuilder historyBuilder = new StringBuilder();
        if (recentEntries.isEmpty()) {
            historyBuilder.append("(sem historial de tarefas anteriores)");
        } else {
            recentEntries.forEach(e -> e.getTasks().stream().limit(3).forEach(t ->
                    historyBuilder.append("- ").append(t.getTitle())
                            .append(" (impacto: ").append(t.getImpactScore()).append("/5)\n")
            ));
        }

        String prompt = """
                És um assistente especializado em gestão de carreira e avaliação de desempenho profissional.

                ## Contexto do utilizador
                - Nome: %s
                - Profissão: %s
                - Objetivo de carreira: %s

                ## REGRA FUNDAMENTAL
                Interpreta SEMPRE as atividades no contexto da profissão indicada acima.
                Uma atividade que parece pessoal ou trivial PODE SER uma tarefa profissional central.
                Exemplos:
                - Para uma Educadora de Infância: "fazer sumo de laranja com as crianças" é uma atividade pedagógica profissional (desenvolvimento sensorial, autonomia, matemática aplicada), NÃO uma atividade doméstica pessoal.
                - Para um Chef: "testei uma nova receita" é desenvolvimento de produto profissional.
                - Para um Personal Trainer: "fiz caminhada com o cliente" é uma sessão de treino profissional.
                Nunca classifiques automaticamente uma atividade como "pessoal" sem considerar o contexto profissional.

                ## Historial recente de tarefas (para contexto)
                %s

                ## Entrada do diário de hoje
                "%s"

                Analisa o texto e extrai as tarefas realizadas. Para cada tarefa:
                1. Cria um título curto e profissionalmente relevante para a profissão "%s".
                2. Melhora a descrição com tom profissional, enquadrando-a no contexto da profissão.
                3. Avalia o impacto profissional de 1 a 5 (considera o contexto da profissão e o objetivo de carreira, não apenas critérios corporativos genéricos).
                4. Escreve uma justificação de uma linha para essa nota de impacto, referindo como a atividade contribui para o papel profissional ou o objetivo de carreira.
                5. Cria até 3 tags relevantes para a profissão (ex: para educadores use tags como AtividadePedagógica, DesenvolvimentoInfantil, EngajamentoFamiliar).

                Responde em Português de Portugal.

                %s
                """.formatted(name, profession, goal, historyBuilder, text, profession, formatInstructions);

        String response = chatClient.prompt().user(prompt).call().content();
        return converter.convert(response);
    }

    private int calculateCurrentStreak(List<LocalDate> datesDesc) {
        if (datesDesc.isEmpty()) return 0;
        LocalDate today = LocalDate.now();
        LocalDate first = datesDesc.get(0);
        // Streak breaks if there's no entry today or yesterday
        if (!first.equals(today) && !first.equals(today.minusDays(1))) return 0;
        int streak = 0;
        LocalDate expected = first;
        for (LocalDate date : datesDesc) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private int calculateLongestStreak(List<LocalDate> datesDesc) {
        if (datesDesc.isEmpty()) return 0;
        List<LocalDate> sorted = new ArrayList<>(datesDesc);
        Collections.sort(sorted); // ASC
        int longest = 1, current = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                current++;
                longest = Math.max(longest, current);
            } else if (!sorted.get(i).equals(sorted.get(i - 1))) {
                current = 1;
            }
        }
        return longest;
    }

    private List<WeeklyProgressDTO> calculateWeeklyBreakdown(List<DailyEntry> allEntries, LocalDate today) {
        List<WeeklyProgressDTO> weeks = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate weekEnd = today.minusWeeks(i);
            LocalDate weekStart = weekEnd.minusDays(6);
            List<TaskLog> weekTasks = allEntries.stream()
                    .filter(e -> !e.getEntryDate().isBefore(weekStart) && !e.getEntryDate().isAfter(weekEnd))
                    .flatMap(e -> e.getTasks().stream())
                    .collect(Collectors.toList());
            double weekAvg = weekTasks.stream().mapToInt(TaskLog::getImpactScore).average().orElse(0.0);
            weeks.add(new WeeklyProgressDTO(weekStart, Math.round(weekAvg * 100.0) / 100.0, weekTasks.size()));
        }
        return weeks;
    }

    private String calculateTrend(List<WeeklyProgressDTO> weeks) {
        if (weeks.size() < 4) return "STABLE";
        int size = weeks.size();
        double recent = weeks.subList(size - 2, size).stream()
                .mapToDouble(WeeklyProgressDTO::averageScore).average().orElse(0);
        double previous = weeks.subList(size - 4, size - 2).stream()
                .mapToDouble(WeeklyProgressDTO::averageScore).average().orElse(0);
        if (recent > previous + 0.3) return "IMPROVING";
        if (recent < previous - 0.3) return "DECLINING";
        return "STABLE";
    }

    private DailyEntryDTO convertToDTO(DailyEntry entry) {
        List<TaskLogDTO> taskDTOs = entry.getTasks().stream()
                .map(this::convertTaskToDTO).toList();
        return new DailyEntryDTO(entry.getId(), entry.getEntryDate(), entry.getRawText(),
                entry.isAiProcessed(), taskDTOs, entry.getMood());
    }

    private TaskLogDTO convertTaskToDTO(TaskLog task) {
        return new TaskLogDTO(task.getId(), task.getTitle(), task.getDescription(),
                task.getImpactScore(), task.getImpactJustification(),
                task.getTags().stream().map(Tag::getName).toList());
    }

    private Tag getOrCreateTag(String tagName) {
        // Normalize: strip #, trim whitespace, lowercase for consistent storage
        String cleanName = tagName.trim().replace("#", "").toLowerCase();
        if (cleanName.isBlank()) return null;
        Optional<Tag> existing = tagRepository.findByNameIgnoreCase(cleanName);
        if (existing.isPresent()) return existing.get();
        try {
            return tagRepository.saveAndFlush(Tag.builder().name(cleanName).build());
        } catch (DataIntegrityViolationException e) {
            return tagRepository.findByNameIgnoreCase(cleanName)
                    .orElseThrow(() -> new IllegalStateException("Erro inesperado com a tag."));
        }
    }
}

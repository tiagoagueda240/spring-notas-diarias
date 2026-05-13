package com.tiagoagueda.api.journal;

import com.tiagoagueda.api.journal.dto.*;
import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.journal.entity.DailyEntry;
import com.tiagoagueda.api.journal.entity.Tag;
import com.tiagoagueda.api.journal.entity.TaskLog;
import com.tiagoagueda.api.journal.repository.DailyEntryRepository;
import com.tiagoagueda.api.journal.repository.TagRepository;
import com.tiagoagueda.api.journal.repository.TaskLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.*;

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

    public DailyEntryDTO saveEntry(String rawText, AppUser currentUser) {
        log.info("A criar nova entrada de diário para o utilizador: {}", currentUser.getEmail());

        DailyEntry newEntry = DailyEntry.builder()
                .entryDate(LocalDate.now())
                .rawText(rawText)
                .build();
        newEntry.setAppUser(currentUser);

        DailyEntry savedEntry = dailyEntryRepository.save(newEntry);

        try {
            log.info("A contactar o Gemini (Spring AI) para extrair tarefas...");
            AiExtractedDailyLog extractedData = extractTasksWithAI(rawText);

            if (extractedData != null && extractedData.tasks() != null) {
                transactionTemplate.executeWithoutResult(status -> {
                    for (AiTask aiTask : extractedData.tasks()) {
                        TaskLog taskLog = new TaskLog();
                        taskLog.setTitle(aiTask.title());
                        taskLog.setDescription(aiTask.description());
                        taskLog.setImpactScore(aiTask.impactScore());
                        taskLog.setImpactJustification(aiTask.impactJustification());

                        Set<Tag> taskTags = new HashSet<>(); // Changed to Set
                        if (aiTask.tags() != null) {
                            for (String tagName : aiTask.tags()) {
                                taskTags.add(getOrCreateTag(tagName));
                            }
                        }
                        taskLog.setTags(taskTags);
                        savedEntry.addTask(taskLog);
                        taskLogRepository.save(taskLog);
                    }
                    savedEntry.setAiProcessed(true);
                    dailyEntryRepository.save(savedEntry);
                });
            }
        } catch (Exception e) {
            log.error("Falha ao processar texto com a IA (Gemini). Erro: {}", e.getMessage(), e);
        }

        return convertToDTO(savedEntry);
    }

    public DailyEntryDTO updateEntry(UUID id, String newText, AppUser currentUser) {
        log.info("A atualizar entrada {} do utilizador {}", id, currentUser.getEmail());

        DailyEntry entry = dailyEntryRepository.findByIdAndAppUser(id, currentUser)
                .orElseThrow(() -> new NoSuchElementException("Diário não encontrado ou sem permissão."));

        entry.setRawText(newText);
        entry.setAiProcessed(false);

        taskLogRepository.deleteAll(entry.getTasks());
        entry.getTasks().clear();

        DailyEntry updatedEntry = dailyEntryRepository.save(entry);

        try {
            AiExtractedDailyLog extractedData = extractTasksWithAI(newText);
            if (extractedData != null && extractedData.tasks() != null) {
                transactionTemplate.executeWithoutResult(status -> {
                    for (AiTask aiTask : extractedData.tasks()) {
                        TaskLog taskLog = new TaskLog();
                        taskLog.setTitle(aiTask.title());
                        taskLog.setDescription(aiTask.description());
                        taskLog.setImpactScore(aiTask.impactScore());
                        taskLog.setImpactJustification(aiTask.impactJustification());

                        Set<Tag> taskTags = new HashSet<>(); // Changed to Set
                        if (aiTask.tags() != null) {
                            for (String tagName : aiTask.tags()) {
                                taskTags.add(getOrCreateTag(tagName));
                            }
                        }
                        taskLog.setTags(taskTags);
                        updatedEntry.addTask(taskLog);
                        taskLogRepository.save(taskLog);
                    }
                    updatedEntry.setAiProcessed(true);
                    dailyEntryRepository.save(updatedEntry);
                });
            }
        } catch (Exception e) {
            log.error("Falha ao reprocessar texto com a IA no Update: {}", e.getMessage());
        }

        return convertToDTO(updatedEntry);
    }

    public TaskLogDTO updateTask(UUID taskId, TaskLogUpdateRequest request, AppUser currentUser) {
        log.info("O utilizador {} está a tentar editar a tarefa {}", currentUser.getEmail(), taskId);

        TaskLog task = taskLogRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Tarefa não encontrada."));

        if (!task.getDailyEntry().getAppUser().getId().equals(currentUser.getId())) {
            throw new NoSuchElementException("Tarefa não encontrada.");
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setImpactScore(request.impactScore());
        task.setImpactJustification(request.impactJustification());

        Set<Tag> updatedTags = new HashSet<>(); // Changed to Set
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
                .orElseThrow(() -> new NoSuchElementException("Diário não encontrado ou sem permissão."));

        TaskLog taskLog = new TaskLog();
        taskLog.setTitle(request.title());
        taskLog.setDescription(request.description());
        taskLog.setImpactScore(request.impactScore());
        taskLog.setImpactJustification(request.impactJustification());

        Set<Tag> taskTags = new HashSet<>(); // Changed to Set
        if (request.tags() != null) {
            for (String tagName : request.tags()) {
                taskTags.add(getOrCreateTag(tagName));
            }
        }
        taskLog.setTags(taskTags);

        entry.addTask(taskLog);
        taskLogRepository.save(taskLog);

        return convertTaskToDTO(taskLog);
    }

    public void deleteTask(UUID taskId, AppUser currentUser) {
        TaskLog task = taskLogRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Tarefa não encontrada."));

        if (!task.getDailyEntry().getAppUser().getId().equals(currentUser.getId())) {
            throw new NoSuchElementException("Tarefa não encontrada.");
        }

        taskLogRepository.delete(task);
    }

    public PerformanceReviewResponse generatePerformanceReview(AppUser currentUser, LocalDate startDate, LocalDate endDate) {
        List<DailyEntry> entries = dailyEntryRepository
                .findByAppUserAndEntryDateBetweenOrderByEntryDateAsc(currentUser, startDate, endDate);

        if (entries.isEmpty()) {
            return new PerformanceReviewResponse("Não existem registos suficientes neste período para gerar um relatório.");
        }

        StringBuilder workData = new StringBuilder();
        entries.forEach(entry -> {
            entry.getTasks().forEach(task -> {
                workData.append(String.format("- [%s] %s (Impacto: %d/5) - Justificação: %s\n",
                        entry.getEntryDate().toString(),
                        task.getTitle(),
                        task.getImpactScore(),
                        task.getImpactJustification()
                ));
            });
        });

        String systemPrompt = """
            Atuas como um conselheiro de carreira executivo e especialista em recursos humanos.
            O teu cliente quer pedir um aumento salarial e forneceu-te o registo das suas tarefas e impacto nos últimos meses.
            
            Com base na lista de tarefas abaixo, escreve um relatório de avaliação de desempenho persuasivo, altamente profissional e bem estruturado.
            
            O documento deve conter:
            1. **Resumo Executivo**: Um parágrafo de impacto sobre o valor trazido à empresa.
            2. **Principais Entregas e Impacto**: Agrupa as tarefas por temas (ex: Resolução de Problemas, Entrega de Produto, Liderança) e destaca as que têm Score de Impacto 4 ou 5.
            3. **Consistência e Fiabilidade**: Menciona a cadência de entregas.
            4. **Proposta de Valor Final**: Um argumento de fecho sólido justificando a progressão salarial ou de carreira.
            
            Usa formatação Markdown (headings, bullets, bold). Evita linguagem arrogante, mas sê extremamente confiante e orientado a dados.
            
            DADOS DE TRABALHO DO COLABORADOR:
            %s
            """;

        String finalPrompt = String.format(systemPrompt, workData.toString());

        String aiReport = chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();

        return new PerformanceReviewResponse(aiReport);
    }

    private AiExtractedDailyLog extractTasksWithAI(String text) {
        var converter = new BeanOutputConverter<>(AiExtractedDailyLog.class);
        String formatInstructions = converter.getFormat();

        String prompt = """
            És um assistente de gestão de carreira e avaliação de desempenho.
            O utilizador escreveu o seguinte diário de trabalho:
            
            "%s"
            
            Analisa o texto e extrai as tarefas realizadas. Para cada tarefa:
            1. Cria um título curto.
            2. Melhora a descrição com um tom profissional.
            3. Avalia o impacto no negócio de 1 a 5.
            4. Escreve uma justificação de uma linha para essa nota de impacto.
            5. Cria até 3 tags relevantes (ex: #BugFix, #Liderança).
            
            %s
            """;

        String finalPrompt = String.format(prompt, text, formatInstructions);

        String response = chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();

        return converter.convert(response);
    }

    private DailyEntryDTO convertToDTO(DailyEntry entry) {
        List<TaskLogDTO> taskDTOs = entry.getTasks().stream()
                .map(this::convertTaskToDTO).toList();

        return new DailyEntryDTO(
                entry.getId(),
                entry.getEntryDate(),
                entry.getRawText(),
                entry.isAiProcessed(),
                taskDTOs
        );
    }

    private TaskLogDTO convertTaskToDTO(TaskLog task) {
        return new TaskLogDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getImpactScore(),
                task.getImpactJustification(),
                task.getTags().stream().map(Tag::getName).toList()
        );
    }

    public Page<DailyEntryDTO> findAllEntries(Pageable pageable, AppUser currentUser) {
        return dailyEntryRepository.findByAppUserOrderByEntryDateDesc(currentUser, pageable)
                .map(this::convertToDTO);
    }

    public Optional<DailyEntryDTO> findById(UUID id) {
        return dailyEntryRepository.findById(id).map(this::convertToDTO);
    }

    public void deleteEntry(UUID id, AppUser currentUser) {
        DailyEntry entry = dailyEntryRepository.findByIdAndAppUser(id, currentUser)
                .orElseThrow(() -> new NoSuchElementException("Diário não encontrado ou não tens permissão para o apagar."));

        dailyEntryRepository.delete(entry);
    }

    private Tag getOrCreateTag(String tagName) {
        String cleanTagName = tagName.trim().replace("#", "");

        Optional<Tag> existingTag = tagRepository.findByNameIgnoreCase(cleanTagName);
        if (existingTag.isPresent()) {
            return existingTag.get();
        }

        try {
            Tag newTag = Tag.builder().name(cleanTagName).build();
            return tagRepository.saveAndFlush(newTag);
        } catch (DataIntegrityViolationException e) {
            return tagRepository.findByNameIgnoreCase(cleanTagName)
                    .orElseThrow(() -> new IllegalStateException("Erro inesperado com a tag."));
        }
    }
}
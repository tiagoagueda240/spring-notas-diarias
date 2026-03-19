package com.tiagoagueda.api.journal;

import com.tiagoagueda.api.journal.dto.AiExtractedDailyLog;
import com.tiagoagueda.api.journal.dto.AiTask; // IMPORTANTE: Importar o dto AiTask
import com.tiagoagueda.api.journal.dto.DailyEntryDTO;
import com.tiagoagueda.api.journal.dto.TaskLogDTO;
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

@Service // Diz ao Spring que esta classe contém a lógica de negócio
public class DailyEntryService {

    private final DailyEntryRepository dailyEntryRepository;
    private final TaskLogRepository taskLogRepository;
    private final TagRepository tagRepository;
    private final ChatClient chatClient;
    private final TransactionTemplate transactionTemplate;

    private static final Logger log = LoggerFactory.getLogger(DailyEntryService.class);

    // Construtor com Injeção de Dependências
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

    /**
     * Lógica Principal: Guarda o texto do utilizador e envia para a IA.
     * NÃO usamos @Transactional aqui em cima propositadamente!
     * Se usássemos, a ligação à Base de Dados ficaria presa à espera que a IA da Google
     * respondesse (o que pode demorar 2 a 5 segundos). Isso destruiria a performance da API.
     */
    public DailyEntryDTO saveEntry(String rawText, AppUser currentUser) {
        log.info("A criar nova entrada de diário para o utilizador: {}", currentUser.getEmail());

        DailyEntry newEntry = new DailyEntry(LocalDate.now(), rawText);
        newEntry.setAppUser(currentUser);

        // 1. Guardamos o diário. O repository.save() abre a sua própria mini-transação rápida.
        DailyEntry savedEntry = dailyEntryRepository.save(newEntry);

        try {
            log.info("A contactar o Gemini (Spring AI) para extrair tarefas...");

            // 2. Chamada à IA demorada (A BD está livre para outros utilizadores neste momento)
            AiExtractedDailyLog extractedData = extractTasksWithAI(rawText);

            if (extractedData != null && extractedData.tasks() != null) {
                log.info("A IA extraiu {} tarefas. A guardar na base de dados...", extractedData.tasks().size());

                // 3. Usamos o TransactionTemplate para abrir uma transação SÓ para o momento de gravar as tarefas.
                // Assim garantimos que ou grava todas as tarefas da IA, ou não grava nenhuma se der erro (Rollback).
                transactionTemplate.executeWithoutResult(status -> {
                    for (AiTask aiTask : extractedData.tasks()) {
                        TaskLog taskLog = new TaskLog();
                        taskLog.setTitle(aiTask.title());
                        taskLog.setDescription(aiTask.description());
                        taskLog.setImpactScore(aiTask.impactScore());
                        taskLog.setImpactJustification(aiTask.impactJustification());

                        // Busca tag existente ou cria uma nova lidando com Race Conditions
                        List<Tag> taskTags = new ArrayList<>();
                        if (aiTask.tags() != null) {
                            for (String tagName : aiTask.tags()) {
                                Tag tag = getOrCreateTag(tagName);
                                taskTags.add(tag);
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
            log.info("✅ Diário guardado e processado pela IA com sucesso!");

        } catch (Exception e) {
            log.error("Falha ao processar texto com a IA (Gemini). Erro: {}", e.getMessage(), e);
        }

        return convertToDTO(savedEntry);
    }



    private AiExtractedDailyLog extractTasksWithAI(String text) {
        // Configuramos o conversor que vai gerar as instruções de formatação JSON para o Gemini
        var converter = new BeanOutputConverter<>(AiExtractedDailyLog.class);
        String formatInstructions = converter.getFormat();

        // O Prompt principal para a IA
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

        // Faz o pedido ao Gemini
        String response = chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();

        // Converte a string JSON de volta para o nosso Record Java!
        return converter.convert(response);
    }

    // Método auxiliar para converter Entidade em DTO (evita loops)
    private DailyEntryDTO convertToDTO(DailyEntry entry) {
        List<TaskLogDTO> taskDTOs = entry.getTasks().stream()
                .map(task -> new TaskLogDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getImpactScore(),
                        task.getImpactJustification(),
                        task.getTags().stream().map(tag -> tag.getName()).toList()
                )).toList();

        return new DailyEntryDTO(
                entry.getId(),
                entry.getEntryDate(),
                entry.getRawText(),
                entry.isAiProcessed(),
                taskDTOs
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
        // Tenta encontrar o diário garantindo que o dono é o currentUser
        DailyEntry entry = dailyEntryRepository.findByIdAndAppUser(id, currentUser)
                .orElseThrow(() -> new NoSuchElementException("Diário não encontrado ou não tens permissão para o apagar."));

        // Se chegou aqui, é seguro apagar!
        dailyEntryRepository.delete(entry);
        log.info("Diário {} apagado com sucesso pelo utilizador {}", id, currentUser.getEmail());
    }

    private Tag getOrCreateTag(String tagName) {
        String cleanTagName = tagName.trim().replace("#", "");

        // 1. Primeira tentativa: Procurar a tag na BD
        Optional<Tag> existingTag = tagRepository.findByNameIgnoreCase(cleanTagName);
        if (existingTag.isPresent()) {
            return existingTag.get();
        }

        // 2. Se não encontrou, tenta criar e gravar
        try {
            Tag newTag = new Tag(cleanTagName);
            // Temos de usar saveAndFlush para forçar a query SQL a ir à BD imediatamente
            // e disparar a exceção de integridade caso haja conflito.
            return tagRepository.saveAndFlush(newTag);

        } catch (DataIntegrityViolationException e) {
            // 3. 💥 Ocorreu uma Race Condition! Outra thread gravou a tag no último milissegundo.
            // Sem pânico. Como já lá está, voltamos a ir buscá-la.
            log.info("Conflito evitado ao criar a tag {}. A ir buscar a tag já existente...", cleanTagName);

            return tagRepository.findByNameIgnoreCase(cleanTagName)
                    .orElseThrow(() -> new IllegalStateException("Erro inesperado: A tag falhou ao gravar, mas não foi encontrada depois."));
        }
    }
}
package com.tiagoagueda.api.journal;

import com.tiagoagueda.api.core.dto.PageResponse;
import com.tiagoagueda.api.core.exception.ErrorResponse;
import com.tiagoagueda.api.journal.dto.*;
import com.tiagoagueda.api.user.AppUser;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/daily-entries")
@RequiredArgsConstructor
@Tag(name = "DiÃ¡rio", description = "Endpoints para gerir entradas de diÃ¡rio, estatÃ­sticas e relatÃ³rios")
@SecurityRequirement(name = "bearerAuth")
public class DailyEntryController {

    private final DailyEntryService service;

    @PostMapping
    @Operation(summary = "Criar entrada de diÃ¡rio", description = "Guarda o texto diÃ¡rio do utilizador e processa tarefas com IA.")
    public ResponseEntity<DailyEntryDTO> createEntry(
            @Valid @RequestBody DailyEntryRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveEntry(request.text(), request.mood(), currentUser));
    }

    @GetMapping
    @Operation(summary = "Listar entradas paginadas", description = "Devolve entradas do utilizador com suporte a paginaÃ§Ã£o (page, size).")
    public ResponseEntity<PageResponse<DailyEntryDTO>> getAllEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        Page<DailyEntryDTO> springPage = service.findAllEntries(PageRequest.of(page, size), currentUser);
        return ResponseEntity.ok(PageResponse.of(springPage));
    }
    @GetMapping("/search")
    @Operation(summary = "Pesquisar e filtrar entradas",
            description = "Filtra entradas por texto livre (q), tags, score mínimo e intervalo de datas. Todos os parâmetros são opcionais.")
    public ResponseEntity<PageResponse<DailyEntryDTO>> searchEntries(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryDate"));
        Page<DailyEntryDTO> result = service.searchEntries(currentUser, q, tags, minScore, from, to, pageable);
        return ResponseEntity.ok(PageResponse.of(result));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Obter entrada por ID")
    public ResponseEntity<DailyEntryDTO> getEntryById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return service.findByIdForUser(id, currentUser).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar entrada", description = "Atualiza o texto e reprocessa automaticamente as tarefas com IA.")
    public ResponseEntity<DailyEntryDTO> updateEntry(
            @PathVariable UUID id,
            @Valid @RequestBody DailyEntryUpdateRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(service.updateEntry(id, request.text(), request.mood(), currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Apagar entrada por ID")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        service.deleteEntry(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Reprocessar IA sem alterar texto
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/reprocess")
    @Operation(summary = "Reprocessar entrada com IA",
            description = "Re-analisa o texto existente com IA sem alterar o conteÃºdo. Ãštil quando o modelo melhora ou o utilizador quer re-analisar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entrada reprocessada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Entrada nÃ£o encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit de IA atingido")
    })
    public ResponseEntity<DailyEntryDTO> reprocessEntry(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(service.reprocessEntry(id, currentUser));
    }

    // -------------------------------------------------------------------------
    // Tarefas
    // -------------------------------------------------------------------------

    @PutMapping("/tasks/{taskId}")
    @Operation(summary = "Editar tarefa manualmente")
    public ResponseEntity<TaskLogDTO> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskLogUpdateRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(service.updateTask(taskId, request, currentUser));
    }

    @PostMapping("/{id}/tasks")
    @Operation(summary = "Adicionar tarefa manual")
    public ResponseEntity<TaskLogDTO> addTaskManually(
            @PathVariable UUID id,
            @Valid @RequestBody TaskLogUpdateRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addTaskManually(id, request, currentUser));
    }

    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "Apagar tarefa")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        service.deleteTask(taskId, currentUser);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------    // Batch: mÃºltiplos dias numa chamada
    // -------------------------------------------------------------------------

    @PostMapping("/batch")
    @Operation(summary = "Obter entradas em batch",
            description = "Devolve entradas para uma lista de datas (mÃ¡x 31). Substitui N chamadas paralelas pelo frontend.")
    public ResponseEntity<List<BatchEntryDTO>> getBatchEntries(
            @Valid @RequestBody BatchEntriesRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(service.getBatchEntries(request.dates(), currentUser));
    }

    // -------------------------------------------------------------------------
    // EstatÃ­sticas calculadas no servidor
    // -------------------------------------------------------------------------

    @GetMapping("/stats")
    @Operation(summary = "EstatÃ­sticas do diÃ¡rio",
            description = "Devolve streak atual, streak mÃ¡ximo, score mÃ©dio e contagem de tarefas de alto impacto calculados no servidor.")
    public ResponseEntity<JournalStatsDTO> getStats(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(service.getJournalStats(currentUser));
    }

    // -------------------------------------------------------------------------
    // Calendar Heatmap
    // -------------------------------------------------------------------------

    @GetMapping("/calendar")
    @Operation(summary = "Heatmap de calendário",
            description = "Devolve um registo por dia com score médio, número de entradas e humor. " +
                    "Ideal para visualização estilo GitHub. Por omissão devolve o ano atual.")
    public ResponseEntity<List<CalendarDayDTO>> getCalendar(
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        int targetYear = (year != null) ? year : java.time.LocalDate.now().getYear();
        return ResponseEntity.ok(service.getCalendarHeatmap(currentUser, targetYear));
    }

    // -------------------------------------------------------------------------
    // RelatÃ³rio IA
    // -------------------------------------------------------------------------

    @PostMapping("/generate-review")
    @Operation(summary = "Gerar relatÃ³rio de desempenho com datas explÃ­citas",
            description = "Gera um relatÃ³rio persuasivo com IA para um perÃ­odo com datas explÃ­citas.")
    @ApiResponse(responseCode = "429", description = "Rate limit de IA atingido")
    public ResponseEntity<PerformanceReviewResponse> generateReview(
            @Valid @RequestBody PerformanceReviewRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(service.generatePerformanceReview(currentUser, request.startDate(), request.endDate()));
    }

    @PostMapping("/generate-review/period")
    @Operation(summary = "Gerar relatÃ³rio de desempenho por perÃ­odo",
            description = "Gera relatÃ³rio IA para WEEK (7 dias), MONTH (30 dias) ou QUARTER (90 dias). Rate-limited a 10 chamadas/hora.")
    @ApiResponse(responseCode = "429", description = "Rate limit de IA atingido")
    public ResponseEntity<PerformanceReviewResponse> generateReviewByPeriod(
            @Valid @RequestBody ReviewPeriodRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(service.generateReviewByPeriod(currentUser, request.period()));
    }

    // -------------------------------------------------------------------------
    // Exportar Brag Document
    // -------------------------------------------------------------------------

    @GetMapping(value = "/export/brag-document", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Exportar Brag Document",
            description = "Gera um documento Markdown completo com todas as conquistas no perÃ­odo. Muito Ãºtil para avaliaÃ§Ãµes de desempenho.")
    public ResponseEntity<String> exportBragDocument(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        String content = service.generateBragDocument(currentUser, startDate, endDate);
        String filename = "brag-document-" + startDate + "-to-" + endDate + ".md";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(content);
    }
}

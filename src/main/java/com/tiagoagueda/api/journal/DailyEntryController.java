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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/daily-entries")
@RequiredArgsConstructor
@Tag(name = "Diário", description = "Endpoints para gerir entradas de diário e relatórios de performance")
@SecurityRequirement(name = "bearerAuth")
public class DailyEntryController {

    private final DailyEntryService service;

    @PostMapping
    @Operation(summary = "Criar entrada de diário", description = "Guarda o texto diário do utilizador e processa tarefas com IA.")
    public ResponseEntity<DailyEntryDTO> createEntry(
            @Valid @RequestBody DailyEntryRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        DailyEntryDTO savedEntryDTO = service.saveEntry(request.text(), currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntryDTO);
    }

    @GetMapping
    @Operation(summary = "Listar entradas de diário", description = "Devolve entradas paginadas do utilizador autenticado.")
    public ResponseEntity<PageResponse<DailyEntryDTO>> getAllEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        Page<DailyEntryDTO> springPage = service.findAllEntries(PageRequest.of(page, size), currentUser);
        return ResponseEntity.ok(PageResponse.of(springPage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter entrada por ID", description = "Devolve uma entrada específica do diário.")
    public ResponseEntity<DailyEntryDTO> getEntryById(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar entrada de diário", description = "Atualiza o texto de uma entrada e reprocessa as tarefas com IA.")
    public ResponseEntity<DailyEntryDTO> updateEntry(
            @PathVariable UUID id,
            @Valid @RequestBody DailyEntryUpdateRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        DailyEntryDTO updatedEntry = service.updateEntry(id, request.text(), currentUser);
        return ResponseEntity.ok(updatedEntry);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Apagar entrada por ID", description = "Remove uma entrada de diário do utilizador autenticado.")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        service.deleteEntry(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tasks/{taskId}")
    @Operation(summary = "Editar Tarefa Manualmente", description = "Permite corrigir os dados de uma tarefa (Impacto, Título).")
    public ResponseEntity<TaskLogDTO> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskLogUpdateRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        TaskLogDTO updatedTask = service.updateTask(taskId, request, currentUser);
        return ResponseEntity.ok(updatedTask);
    }

    @PostMapping("/{id}/tasks")
    @Operation(summary = "Adicionar Tarefa Manual", description = "Adiciona uma tarefa diretamente a um dia específico sem usar IA.")
    public ResponseEntity<TaskLogDTO> addTaskManually(
            @PathVariable UUID id,
            @Valid @RequestBody TaskLogUpdateRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        TaskLogDTO newTask = service.addTaskManually(id, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "Apagar Tarefa", description = "Remove uma tarefa específica sem apagar o dia inteiro.")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        service.deleteTask(taskId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate-review")
    @Operation(summary = "Gerar Relatório de Desempenho", description = "Gera um relatório persuasivo com IA para pedidos de aumento salarial.")
    public ResponseEntity<PerformanceReviewResponse> generateReview(
            @Valid @RequestBody PerformanceReviewRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        PerformanceReviewResponse response = service.generatePerformanceReview(
                currentUser,
                request.startDate(),
                request.endDate()
        );
        return ResponseEntity.ok(response);
    }
}
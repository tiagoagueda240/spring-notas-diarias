package com.tiagoagueda.api.journal;

import com.tiagoagueda.api.core.dto.PageResponse;
import com.tiagoagueda.api.core.exception.ErrorResponse;
import com.tiagoagueda.api.journal.dto.DailyEntryDTO;
import com.tiagoagueda.api.journal.dto.DailyEntryRequest;
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

@RestController // Diz que isto é uma API REST (vai devolver JSON)
@RequestMapping("/api/v1/daily-entries") // O URL base para este controller
@RequiredArgsConstructor
@Tag(name = "Diário", description = "Endpoints para gerir entradas de diário")
@SecurityRequirement(name = "bearerAuth")
/**
 * Controller REST para criação, consulta e remoção de entradas de diário.
 */
public class DailyEntryController {

    private final DailyEntryService service;

    /**
     * Endpoint para criar uma nova entrada.
     * @Valid - Valida o corpo do pedido usando as anotações do DTO (@NotBlank, etc).
     * @AuthenticationPrincipal - Magia do Spring Security! Como o JWTFilter já validou o utilizador
     * e colocou-o no SecurityContext, o Spring injeta o objeto 'AppUser' diretamente aqui.
     */
    @PostMapping
    @Operation(summary = "Criar entrada de diário", description = "Guarda o texto diário do utilizador e processa tarefas com IA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Entrada criada com sucesso",
                content = @Content(schema = @Schema(implementation = DailyEntryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no pedido",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<DailyEntryDTO> createEntry( // <-- Agora devolve DailyEntryDTO
                                                      @Valid @RequestBody DailyEntryRequest request,
                                                      @AuthenticationPrincipal AppUser currentUser
    ) {
        DailyEntryDTO savedEntryDTO = service.saveEntry(request.text(), currentUser);
        // Devolvemos Status 201 (Created) em vez do 200 normal, é uma boa prática para criação de recursos.
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntryDTO);
    }

    /**
     * Lista entradas paginadas do utilizador autenticado.
     *
     * page e size são query params com valores default para facilitar uso no frontend.
     */
    @GetMapping
    @Operation(summary = "Listar entradas de diário", description = "Devolve entradas paginadas do utilizador autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista devolvida com sucesso",
            content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<DailyEntryDTO>> getAllEntries( // <-- Agora devolve PageResponse
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size,
                                                                      @AuthenticationPrincipal AppUser currentUser
    ) {
        // 1. Vai buscar a página normal do Spring ao Service
        Page<DailyEntryDTO> springPage = service.findAllEntries(PageRequest.of(page, size), currentUser);

        // 2. Converte para o teu formato limpo usando o nosso método ".of()" e devolve
        return ResponseEntity.ok(PageResponse.of(springPage));
    }

    /**
     * Procura uma entrada por id e devolve 404 se não existir.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obter entrada por ID", description = "Devolve uma entrada específica do diário.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entrada encontrada",
            content = @Content(schema = @Schema(implementation = DailyEntryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Entrada não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DailyEntryDTO> getEntryById(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Apaga uma entrada do utilizador autenticado.
     *
     * O service valida ownership/segurança antes de apagar.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Apagar entrada por ID", description = "Remove uma entrada de diário do utilizador autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Entrada apagada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Entrada não encontrada ou sem permissão",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteEntry( // <-- Repara que agora é <Void>
                                             @PathVariable UUID id,
                                             @AuthenticationPrincipal AppUser currentUser // <-- O Spring injeta o utilizador do Token
    ) {
        // Mandamos apagar com segurança
        service.deleteEntry(id, currentUser);

        // Devolvemos o status HTTP 204 (No Content)
        return ResponseEntity.noContent().build();
    }
}

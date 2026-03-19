package com.tiagoagueda.api.journal;

import com.tiagoagueda.api.core.dto.PageResponse;
import com.tiagoagueda.api.journal.dto.DailyEntryDTO;
import com.tiagoagueda.api.journal.dto.DailyEntryRequest;
import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.journal.entity.DailyEntry;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController // Diz que isto é uma API REST (vai devolver JSON)
@RequestMapping("/api/v1/daily-entries") // O URL base para este controller
public class DailyEntryController {

    private final DailyEntryService service;

    public DailyEntryController(DailyEntryService service) {
        this.service = service;
    }

    /**
     * Endpoint para criar uma nova entrada.
     * @Valid - Valida o corpo do pedido usando as anotações do DTO (@NotBlank, etc).
     * @AuthenticationPrincipal - Magia do Spring Security! Como o JWTFilter já validou o utilizador
     * e colocou-o no SecurityContext, o Spring injeta o objeto 'AppUser' diretamente aqui.
     */
    @PostMapping
    public ResponseEntity<DailyEntryDTO> createEntry( // <-- Agora devolve DailyEntryDTO
                                                      @Valid @RequestBody DailyEntryRequest request,
                                                      @AuthenticationPrincipal AppUser currentUser
    ) {
        DailyEntryDTO savedEntryDTO = service.saveEntry(request.text(), currentUser);
        // Devolvemos Status 201 (Created) em vez do 200 normal, é uma boa prática para criação de recursos.
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntryDTO);
    }

    @GetMapping
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

    @GetMapping("/{id}")
    public ResponseEntity<DailyEntryDTO> getEntryById(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
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

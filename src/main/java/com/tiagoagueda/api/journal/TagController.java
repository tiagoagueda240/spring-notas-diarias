package com.tiagoagueda.api.journal;

import com.tiagoagueda.api.journal.dto.TagWithCountDTO;
import com.tiagoagueda.api.user.AppUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Endpoint para listar tags do utilizador com contagem de uso")
@SecurityRequirement(name = "bearerAuth")
public class TagController {

    private final DailyEntryService dailyEntryService;

    @GetMapping
    @Operation(summary = "Listar tags com contagem",
            description = "Devolve todas as tags usadas pelo utilizador ordenadas por popularidade. " +
                          "Permite pesquisa e filtros ricos sem calcular no cliente.")
    public ResponseEntity<List<TagWithCountDTO>> getTagsWithCount(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(dailyEntryService.getTagsWithCount(currentUser));
    }
}

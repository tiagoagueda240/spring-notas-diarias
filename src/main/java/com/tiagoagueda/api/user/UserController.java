package com.tiagoagueda.api.user;

import com.tiagoagueda.api.journal.DailyEntryService;
import com.tiagoagueda.api.journal.dto.GoalProgressDTO;
import com.tiagoagueda.api.user.dto.AchievementDTO;
import com.tiagoagueda.api.user.dto.UpdateGoalRequest;
import com.tiagoagueda.api.user.dto.UserProfileDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Utilizador", description = "Endpoints para perfil e métricas de progresso")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final AppUserRepository userRepository;
    private final DailyEntryService dailyEntryService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil", description = "Devolve o perfil do utilizador autenticado incluindo objetivo definido.")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(toProfileDTO(currentUser));
    }

    @PutMapping("/me/goal")
    @Operation(summary = "Definir/atualizar objetivo",
            description = "Define ou atualiza o objetivo de carreira do utilizador. Redefine a data de início do objetivo.")
    public ResponseEntity<UserProfileDTO> updateGoal(
            @Valid @RequestBody UpdateGoalRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        currentUser.setGoal(request.goal());
        currentUser.setGoalSetAt(LocalDate.now());
        AppUser saved = userRepository.save(currentUser);
        return ResponseEntity.ok(toProfileDTO(saved));
    }

    @GetMapping("/me/progress")
    @Operation(summary = "Métricas de progresso",
            description = "Calcula % de progresso, tendência (IMPROVING/STABLE/DECLINING) e breakdown semanal das últimas 12 semanas face ao objetivo definido.")
    public ResponseEntity<GoalProgressDTO> getProgress(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(dailyEntryService.calculateGoalProgress(currentUser));
    }

    @GetMapping("/me/achievements")
    @Operation(summary = "Conquistas / Badges",
            description = "Devolve todas as conquistas disponíveis com estado (desbloqueado ou não) e progresso atual.")
    public ResponseEntity<List<AchievementDTO>> getAchievements(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(dailyEntryService.getAchievements(currentUser));
    }

    private UserProfileDTO toProfileDTO(AppUser user) {
        return new UserProfileDTO(user.getId(), user.getName(), user.getEmail(),
                user.getGoal(), user.getGoalSetAt());
    }
}

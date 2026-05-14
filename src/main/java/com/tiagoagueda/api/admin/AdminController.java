package com.tiagoagueda.api.admin;

import com.tiagoagueda.api.admin.dto.AdminUserDTO;
import com.tiagoagueda.api.admin.dto.UpdatePlanRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Gestão de contas — acesso restrito a administradores")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Listar utilizadores", description = "Devolve todos os utilizadores com métricas de uso.")
    public ResponseEntity<List<AdminUserDTO>> listUsers() {
        return ResponseEntity.ok(adminService.listAllUsers());
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Eliminar conta", description = "Remove permanentemente um utilizador e todos os seus dados.")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/plan")
    @Operation(summary = "Alterar plano", description = "Muda o plano de subscrição de um utilizador (FREE, PRO).")
    public ResponseEntity<AdminUserDTO> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        return ResponseEntity.ok(adminService.updatePlan(id, request));
    }
}

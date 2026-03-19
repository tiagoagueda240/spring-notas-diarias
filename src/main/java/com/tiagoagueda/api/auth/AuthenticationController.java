package com.tiagoagueda.api.auth;

import com.tiagoagueda.api.auth.dto.AuthenticationRequest;
import com.tiagoagueda.api.auth.dto.AuthenticationResponse;
import com.tiagoagueda.api.auth.dto.RegisterRequest;
import com.tiagoagueda.api.core.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para registo e login")
/**
 * Controller responsável pelos endpoints públicos de autenticação.
 *
 * Aqui ficam apenas operações de entrada/saída HTTP (DTOs e status code).
 * A regra de negócio de autenticação fica no AuthenticationService.
 */
public class AuthenticationController {

    private final AuthenticationService service;

    /**
     * Regista um novo utilizador e devolve um JWT válido para uso imediato.
     */
    @PostMapping("/register")
    @Operation(summary = "Registar novo utilizador", description = "Cria uma nova conta e devolve token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizador registado com sucesso",
                content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no pedido",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email já registado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    /**
     * Autentica um utilizador existente e devolve um novo JWT.
     */
    @PostMapping("/authenticate")
    @Operation(summary = "Autenticar utilizador", description = "Valida credenciais e devolve token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação com sucesso",
                content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no pedido",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
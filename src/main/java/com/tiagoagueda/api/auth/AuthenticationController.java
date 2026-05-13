package com.tiagoagueda.api.auth;

import com.tiagoagueda.api.auth.dto.*;
import com.tiagoagueda.api.core.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints públicos de autenticação e gestão de sessão")
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    @Operation(summary = "Registar novo utilizador", description = "Cria uma nova conta e devolve access token (15 min) e refresh token (7 dias).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizador registado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email já registado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Autenticar utilizador", description = "Valida credenciais e devolve access token (15 min) e refresh token (7 dias).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Renovar access token",
            description = "Usa o refresh token opaco para obter um novo access token. O refresh token é rotacionado a cada utilização.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou revogado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(service.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Terminar sessão", description = "Revoga o refresh token do dispositivo atual.")
    @ApiResponse(responseCode = "204", description = "Sessão terminada com sucesso")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        service.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Esqueci a password",
            description = "Envia um link de reset por email (válido 15 min). Por segurança, responde sempre com 200 independentemente de o email existir.")
    @ApiResponse(responseCode = "200", description = "Pedido processado (verifique o email se a conta existir)")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        service.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir password", description = "Define uma nova password usando o token recebido no email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password redefinida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        service.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}

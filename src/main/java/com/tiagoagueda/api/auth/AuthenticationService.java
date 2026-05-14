package com.tiagoagueda.api.auth;

import com.tiagoagueda.api.auth.dto.*;
import com.tiagoagueda.api.auth.entity.PasswordResetToken;
import com.tiagoagueda.api.auth.entity.RefreshToken;
import com.tiagoagueda.api.auth.repository.PasswordResetTokenRepository;
import com.tiagoagueda.api.auth.repository.RefreshTokenRepository;
import com.tiagoagueda.api.core.email.EmailService;
import com.tiagoagueda.api.core.exception.InvalidTokenException;
import com.tiagoagueda.api.core.exception.UserAlreadyExistsException;
import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.user.AppUserRepository;
import com.tiagoagueda.api.user.Role;
import com.tiagoagueda.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${application.security.jwt.refresh-token-expiration-days:7}")
    private int refreshTokenExpirationDays;

    @Value("${application.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("A iniciar processo de registo para o email: {}", request.email());

        if (repository.findByEmail(request.email()).isPresent()) {
            log.warn("O registo falhou. O email {} já está em uso.", request.email());
            throw new UserAlreadyExistsException("Este email já se encontra registado no nosso sistema.");
        }

        AppUser user = AppUser.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        repository.save(user);
        log.info("Utilizador {} registado com sucesso.", request.email());

        String accessToken = jwtService.generateToken(Map.of("role", user.getRole().name()), user);
        RefreshToken refreshToken = createRefreshToken(user);
        return new AuthenticationResponse(accessToken, refreshToken.getId().toString(), user.getRole().name());
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("A tentar autenticar utilizador: {}", request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUser user = repository.findByEmail(request.email()).orElseThrow();
        log.info("Autenticação bem-sucedida para: {}", request.email());

        String accessToken = jwtService.generateToken(Map.of("role", user.getRole().name()), user);
        RefreshToken refreshToken = createRefreshToken(user);
        return new AuthenticationResponse(accessToken, refreshToken.getId().toString(), user.getRole().name());
    }

    /**
     * Valida o refresh token opaco, revoga-o (rotação) e emite novos tokens.
     * A rotação garante que tokens roubados são detetados na próxima utilização.
     */
    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        UUID tokenId = parseTokenUuid(request.refreshToken());

        RefreshToken stored = refreshTokenRepository.findByIdAndRevokedFalse(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido ou revogado."));

        if (Instant.now().isAfter(stored.getExpiresAt())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new InvalidTokenException("Refresh token expirado. Faz login novamente.");
        }

        // Rotação: revogar o atual e criar um novo
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        AppUser user = stored.getUser();
        String newAccessToken = jwtService.generateToken(Map.of("role", user.getRole().name()), user);
        RefreshToken newRefreshToken = createRefreshToken(user);

        log.info("Tokens renovados via refresh para o utilizador: {}", user.getEmail());
        return new AuthenticationResponse(newAccessToken, newRefreshToken.getId().toString(), user.getRole().name());
    }

    /**
     * Revoga o refresh token específico (logout do dispositivo atual).
     */
    @Transactional
    public void logout(RefreshTokenRequest request) {
        try {
            UUID tokenId = parseTokenUuid(request.refreshToken());
            refreshTokenRepository.findByIdAndRevokedFalse(tokenId).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        } catch (InvalidTokenException e) {
            // Token com formato inválido — nada a revogar
        }
        log.info("Logout efetuado.");
    }

    /**
     * Inicia o fluxo de recuperação de password.
     * Não revela se o email existe (anti-enumeração).
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<AppUser> userOpt = repository.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            log.info("Pedido de reset para email inexistente: {} (ignorado por segurança)", request.email());
            return;
        }

        AppUser user = userOpt.get();
        passwordResetTokenRepository.invalidateAllByUserEmail(request.email());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getId();
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        log.info("Reset de password iniciado para: {}", request.email());
    }

    /**
     * Valida o token de reset e define a nova password.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UUID tokenId = parseTokenUuid(request.token());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByIdAndUsedFalse(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Token inválido ou já utilizado."));

        if (Instant.now().isAfter(resetToken.getExpiresAt())) {
            throw new InvalidTokenException("Token expirado. Solicita um novo link de reset.");
        }

        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revogar todos os refresh tokens por segurança
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Password redefinida com sucesso para: {}", user.getEmail());
    }

    private RefreshToken createRefreshToken(AppUser user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .expiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
                .build();
        return refreshTokenRepository.save(token);
    }

    private UUID parseTokenUuid(String token) {
        try {
            return UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Formato do token inválido.");
        }
    }
}

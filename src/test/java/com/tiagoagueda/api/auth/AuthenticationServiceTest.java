package com.tiagoagueda.api.auth;

import com.tiagoagueda.api.auth.dto.AuthenticationResponse;
import com.tiagoagueda.api.auth.dto.RegisterRequest;
import com.tiagoagueda.api.auth.entity.RefreshToken;
import com.tiagoagueda.api.auth.repository.PasswordResetTokenRepository;
import com.tiagoagueda.api.auth.repository.RefreshTokenRepository;
import com.tiagoagueda.api.core.email.EmailService;
import com.tiagoagueda.api.core.exception.UserAlreadyExistsException;
import com.tiagoagueda.api.security.JwtService;
import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private AppUserRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_WhenEmailAlreadyExists_ThrowsUserAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("Nome", "nome@email.com", "123456");
        when(repository.findByEmail("nome@email.com")).thenReturn(Optional.of(new AppUser()));

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Este email já se encontra registado no nosso sistema.");

        verify(repository, never()).save(any(AppUser.class));
    }

    @Test
    void register_WhenRequestIsValid_EncodesPasswordSavesUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("Nome", "nome@email.com", "123456");

        when(repository.findByEmail("nome@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-ok");

        RefreshToken fakeRefreshToken = RefreshToken.builder()
                .user(new AppUser())
                .expiresAt(Instant.now().plusSeconds(604800))
                .build();
        // Simulate auto-generated UUID
        fakeRefreshToken = spy(fakeRefreshToken);
        UUID fakeId = UUID.randomUUID();
        doReturn(fakeId).when(fakeRefreshToken).getId();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(fakeRefreshToken);

        AuthenticationResponse response = authenticationService.register(request);

        ArgumentCaptor<AppUser> savedUserCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(savedUserCaptor.capture());

        AppUser savedUser = savedUserCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Nome");
        assertThat(savedUser.getEmail()).isEqualTo("nome@email.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(response.accessToken()).isEqualTo("jwt-ok");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.refreshToken()).isNotBlank();
    }
}

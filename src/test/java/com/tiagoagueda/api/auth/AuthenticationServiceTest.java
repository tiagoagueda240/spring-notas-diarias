package com.tiagoagueda.api.auth;

import com.tiagoagueda.api.auth.dto.AuthenticationResponse;
import com.tiagoagueda.api.auth.dto.RegisterRequest;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AppUserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

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
    void register_WhenRequestIsValid_EncodesPasswordSavesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Nome", "nome@email.com", "123456");

        when(repository.findByEmail("nome@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-ok");

        AuthenticationResponse response = authenticationService.register(request);

        ArgumentCaptor<AppUser> savedUserCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(savedUserCaptor.capture());

        AppUser savedUser = savedUserCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Nome");
        assertThat(savedUser.getEmail()).isEqualTo("nome@email.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(response.token()).isEqualTo("jwt-ok");
    }
}

package com.tiagoagueda.api.auth;

import com.tiagoagueda.api.auth.dto.AuthenticationRequest;
import com.tiagoagueda.api.auth.dto.AuthenticationResponse;
import com.tiagoagueda.api.auth.dto.RegisterRequest;
import com.tiagoagueda.api.core.exception.UserAlreadyExistsException;
import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.user.AppUserRepository;
import com.tiagoagueda.api.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService(AppUserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        log.info("A iniciar processo de registo para o email: {}", request.email());

        if (repository.findByEmail(request.email()).isPresent()) {
            log.warn("O registo falhou. O email {} já está em uso.", request.email());
            throw new UserAlreadyExistsException("Este email já se encontra registado no nosso sistema.");
        }
        // Cria o utilizador
        AppUser user = new AppUser();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); // NUNCA guardar em plain text!

        // Guarda na BD
        repository.save(user);

        log.info("Utilizador {} registado com sucesso na base de dados.", request.email());

        // Gera o Token JWT e devolve
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("A tentar autenticar utilizador: {}", request.email());

        // O Spring Security faz a validação da password automaticamente aqui.
        // Se a password estiver errada, ele lança um erro e o código para.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Se chegou aqui, o login está correto! Vamos buscar o user à BD.
        AppUser user = repository.findByEmail(request.email()).orElseThrow();

        log.info("Autenticação bem-sucedida. A gerar Token JWT para: {}", request.email());
        // Gera o Token JWT e devolve
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}
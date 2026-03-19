package com.tiagoagueda.api.auth;

import com.tiagoagueda.api.auth.dto.AuthenticationRequest;
import com.tiagoagueda.api.auth.dto.AuthenticationResponse;
import com.tiagoagueda.api.auth.dto.RegisterRequest;
import com.tiagoagueda.api.core.exception.UserAlreadyExistsException;
import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.user.AppUserRepository;
import com.tiagoagueda.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * Serviço com a lógica de registo e login.
 *
 * Responsabilidades principais:
 * - validar regras de negócio (ex: email já existe),
 * - criar utilizador com password encriptada,
 * - pedir autenticação ao Spring Security,
 * - gerar e devolver JWT.
 */
public class AuthenticationService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * Regista um novo utilizador.
     *
     * Fluxo:
     * 1) valida unicidade do email;
     * 2) encripta password;
     * 3) grava utilizador na BD;
     * 4) devolve token JWT.
     */
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("A iniciar processo de registo para o email: {}", request.email());

        if (repository.findByEmail(request.email()).isPresent()) {
            log.warn("O registo falhou. O email {} já está em uso.", request.email());
            throw new UserAlreadyExistsException("Este email já se encontra registado no nosso sistema.");
        }
        // Cria o utilizador
        AppUser user = AppUser.builder()
            .name(request.name())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .build(); // NUNCA guardar em plain text!

        // Guarda na BD
        repository.save(user);

        log.info("Utilizador {} registado com sucesso na base de dados.", request.email());

        // Gera o Token JWT e devolve
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    /**
     * Autentica utilizador existente com AuthenticationManager do Spring.
     *
     * Se as credenciais estiverem corretas, devolve JWT.
     * Se estiverem erradas, o Spring lança exceção (tratada globalmente).
     */
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
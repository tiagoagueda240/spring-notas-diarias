package com.tiagoagueda.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer; // <-- IMPORTANTE ADICIONAR
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // <-- IMPORTANTE ADICIONAR
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração principal do Spring Security.
 * @EnableWebSecurity liga a proteção web do Spring à nossa aplicação.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * O SecurityFilterChain é a cadeia de filtros pela qual todos os pedidos HTTP passam.
     * É aqui que definimos o que é público e o que é privado.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desativamos o CSRF porque vamos usar JWT (Tokens), logo não usamos cookies de sessão.
                .csrf(AbstractHttpConfigurer::disable)

                // STATELESS significa que o servidor não vai guardar o estado do utilizador (sessão).
                // Cada pedido HTTP tem de ser independente e trazer o seu próprio Token JWT.
                .cors(Customizer.withDefaults())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Regras de autorização de URLs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // Login/Registo: Acesso Livre
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll() // <-- LIBERTAR O SWAGGER
                        .requestMatchers("/actuator/**").permitAll()    // Monitorização: Acesso Livre
                        .anyRequest().authenticated()                   // Resto: Exige Token Válido
                )

                // Configura quem verifica o utilizador na BD (authenticationProvider)
                .authenticationProvider(authenticationProvider)

                // Adiciona o nosso filtro JWT ANTES do filtro padrão de Username/Password do Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
package com.tiagoagueda.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filtro que interceta CADA pedido HTTP para validar o Token JWT.
 * Herdar de OncePerRequestFilter garante que este código corre apenas 1 vez por pedido.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. O pedido não tem Token? Passamos a bola ao Spring Security (que vai bloquear o acesso mais à frente)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extrair o Token da string (tirar a palavra "Bearer ")
        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        // 3. Obter o email guardado dentro do Token
        final String userEmail = jwtService.extractUsername(jwt);

        // 4. Se o Token tem um email E o utilizador ainda não está autenticado no contexto atual do servidor
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Vai à Base de Dados buscar o utilizador
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Verifica se o token pertence a este utilizador e se ainda não expirou
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Cria o objeto de autenticação oficial do Spring
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Guarda o utilizador no Contexto de Segurança. A partir deste momento, o utilizador tem "livre-trânsito".
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continua o fluxo normal do pedido HTTP até chegar ao Controller
        filterChain.doFilter(request, response);
    }
}
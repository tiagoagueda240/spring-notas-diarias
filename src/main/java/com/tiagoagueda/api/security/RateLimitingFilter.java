package com.tiagoagueda.api.security;

import com.tiagoagueda.api.user.AppUser;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filtro de rate limiting para endpoints de IA (custosos).
 *
 * Usa Bucket4j em memória por utilizador autenticado.
 * Bounded LRU map (máx 5 000 entradas) para evitar memory leak em instâncias longas.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /**
     * Mapa LRU thread-safe com limite máximo de 5 000 entradas.
     * Remove automaticamente os menos recentemente usados ao ultrapassar o limite.
     */
    private static final int MAX_BUCKETS = 5_000;
    private final Map<String, Bucket> buckets = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Bucket> eldest) {
                    return size() > MAX_BUCKETS;
                }
            }
    );

    @Value("${application.rate-limit.ai-requests-per-hour:10}")
    private int aiRequestsPerHour;

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(aiRequestsPerHour)
                .refillGreedy(aiRequestsPerHour, Duration.ofHours(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (isAiEndpoint(request.getRequestURI())) {
            String key = resolveRateLimitKey(request);
            Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Too Many Requests\"," +
                        "\"message\":\"Limite de pedidos IA atingido. Máximo de " + aiRequestsPerHour + " por hora.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAiEndpoint(String uri) {
        return uri.contains("/generate-review") || uri.contains("/reprocess");
    }

    /**
     * Usa o ID do utilizador autenticado como chave. Fallback para IP se não autenticado.
     */
    private String resolveRateLimitKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return "user:" + user.getId();
        }
        return "ip:" + request.getRemoteAddr();
    }
}

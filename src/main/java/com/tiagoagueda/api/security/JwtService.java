package com.tiagoagueda.api.security;

import io.jsonwebtoken.io.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // NOVA SINTAXE JJWT 0.12.x
        return Jwts.builder()
                .claims(extraClaims) // Antes era setClaims
                .subject(userDetails.getUsername()) // Antes era setSubject
                .issuedAt(new Date(System.currentTimeMillis())) // Antes era setIssuedAt
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Antes era setExpiration
                .signWith(getSignInKey()) // O algoritmo (HS256) agora é inferido automaticamente da chave!
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // NOVA SINTAXE JJWT 0.12.x
        return Jwts.parser()
                .verifyWith(getSignInKey()) // Antes era setSigningKey
                .build()
                .parseSignedClaims(token) // Antes era parseClaimsJws
                .getPayload(); // Antes era getBody
    }

    private SecretKey getSignInKey() { // O retorno mudou de Key para SecretKey
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (DecodingException e) {
            log.warn("Aviso de Segurança: A chave JWT não está em Base64. A usar plain-text.");
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
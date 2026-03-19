package com.tiagoagueda.api.security;

import com.tiagoagueda.api.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String secret = Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes());
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
    }

    @Test
    void generateToken_ThenExtractUsername_AndValidateToken() {
        AppUser user = createUser("nome@email.com", "encoded");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("nome@email.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_WhenTokenBelongsToAnotherUser_ReturnsFalse() {
        AppUser tokenOwner = createUser("owner@email.com", "encoded");
        AppUser anotherUser = createUser("other@email.com", "encoded");

        String token = jwtService.generateToken(tokenOwner);

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    private AppUser createUser(String email, String password) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}

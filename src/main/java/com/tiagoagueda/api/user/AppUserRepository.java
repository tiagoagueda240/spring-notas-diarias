package com.tiagoagueda.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de acesso a dados para utilizadores da aplicação.
 */
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    /**
     * Procura um utilizador pelo email.
     */
    Optional<AppUser> findByEmail(String email);
}

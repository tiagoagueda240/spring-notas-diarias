package com.tiagoagueda.api.auth.repository;

import com.tiagoagueda.api.auth.entity.RefreshToken;
import com.tiagoagueda.api.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByIdAndRevokedFalse(UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken t SET t.revoked = true WHERE t.user = :user")
    void revokeAllByUser(@Param("user") AppUser user);

    @Modifying
    @Transactional
    void deleteAllByUser(AppUser user);
}

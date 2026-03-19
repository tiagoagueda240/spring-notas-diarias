package com.tiagoagueda.api.journal.repository;

import com.tiagoagueda.api.journal.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
/**
 * Repositório de acesso a dados para tags.
 *
 * As tags são reutilizadas por texto normalizado (lowercase + trim) para evitar
 * duplicação semântica e facilitar agregações futuras.
 */
public interface TagRepository extends JpaRepository<Tag, UUID> {
    /**
     * Procura uma tag pelo nome sem diferenciar maiúsculas/minúsculas.
     */
    Optional<Tag> findByNameIgnoreCase(String name);
}
package com.tiagoagueda.api.journal.repository;

import com.tiagoagueda.api.journal.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    // O Spring cria a query SQL automaticamente só por leres este nome!
    Optional<Tag> findByNameIgnoreCase(String name);
}
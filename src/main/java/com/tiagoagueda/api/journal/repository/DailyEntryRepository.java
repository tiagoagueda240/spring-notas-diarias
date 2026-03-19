package com.tiagoagueda.api.journal.repository;

import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.journal.entity.DailyEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
/**
 * Repositório de acesso a dados para DailyEntry.
 *
 * Centraliza queries específicas do diário, com foco em segurança por utilizador
 * e otimização de leitura via EntityGraph.
 */
public interface DailyEntryRepository extends JpaRepository<DailyEntry, UUID> {
    // Só por fazeres "extends JpaRepository", já ganhaste métodos grátis como:
    // save(), findAll(), findById(), deleteById()


    /**
     * @EntityGraph é uma técnica avançada fantástica.
     * Como a relação "tasks" é FetchType.LAZY, o Spring normalmente faria 1 query
     * para trazer o diário, e depois N queries separadas para trazer as tarefas de cada diário.
     * Ao usar attributePaths = {"tasks", "tasks.tags"}, forçamos o Hibernate a fazer um
     * SQL LEFT JOIN, trazendo tudo numa única viagem à Base de Dados.
     */
    @EntityGraph(attributePaths = {"tasks", "tasks.tags"})
    Page<DailyEntry> findByAppUserOrderByEntryDateDesc(AppUser appUser, Pageable pageable);

    /**
     * Procura diário por id validando também o dono (AppUser).
     */
    @EntityGraph(attributePaths = {"tasks", "tasks.tags"})
    Optional<DailyEntry> findByIdAndAppUser(UUID id, AppUser appUser);
}

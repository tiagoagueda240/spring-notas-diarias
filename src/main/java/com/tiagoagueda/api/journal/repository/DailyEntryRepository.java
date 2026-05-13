package com.tiagoagueda.api.journal.repository;

import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.journal.entity.DailyEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyEntryRepository extends JpaRepository<DailyEntry, UUID>, JpaSpecificationExecutor<DailyEntry> {

    @EntityGraph(attributePaths = {"tasks", "tasks.tags"})
    Page<DailyEntry> findByAppUserOrderByEntryDateDesc(AppUser appUser, Pageable pageable);

    @EntityGraph(attributePaths = {"tasks", "tasks.tags"})
    Optional<DailyEntry> findByIdAndAppUser(UUID id, AppUser appUser);

    @EntityGraph(attributePaths = {"tasks", "tasks.tags"})
    List<DailyEntry> findByAppUserAndEntryDateBetweenOrderByEntryDateAsc(AppUser appUser, LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"tasks", "tasks.tags"})
    Optional<DailyEntry> findById(UUID id);

    /** Devolve as datas DISTINTAS de todas as entradas do utilizador ordenadas DESC — usado para calcular streak. */
    @Query("SELECT DISTINCT e.entryDate FROM DailyEntry e WHERE e.appUser = :user ORDER BY e.entryDate DESC")
    List<LocalDate> findAllEntryDatesByUser(@Param("user") AppUser user);

    /** Conta o total de entradas do utilizador. */
    long countByAppUser(AppUser appUser);

    /** Devolve entradas para uma lista específica de datas — usado pelo endpoint batch. */
    @EntityGraph(attributePaths = {"tasks", "tasks.tags"})
    List<DailyEntry> findByAppUserAndEntryDateIn(AppUser appUser, List<LocalDate> dates);

    /** Devolve as últimas N entradas já processadas pela IA — usado para dar contexto histórico ao prompt. */
    @EntityGraph(attributePaths = {"tasks"})
    List<DailyEntry> findTop7ByAppUserAndAiProcessedTrueOrderByEntryDateDesc(AppUser appUser);

    /**
     * Agrega por data para o heatmap de calendário.
     * Devolve [entryDate, avgImpactScore, entryCount, firstMood] para cada dia no intervalo.
     */
    @Query("""
            SELECT e.entryDate,
                   COALESCE(AVG(t.impactScore), 0.0),
                   COUNT(DISTINCT e.id),
                   MAX(e.mood)
            FROM DailyEntry e
            LEFT JOIN e.tasks t
            WHERE e.appUser = :user
              AND e.entryDate >= :start
              AND e.entryDate <= :end
            GROUP BY e.entryDate
            ORDER BY e.entryDate ASC
            """)
    List<Object[]> findCalendarData(
            @Param("user") AppUser user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}

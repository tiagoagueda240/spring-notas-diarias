package com.tiagoagueda.api.journal.repository;

import com.tiagoagueda.api.journal.entity.TaskLog;
import com.tiagoagueda.api.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, UUID> {

    /** Score médio de impacto de todas as tarefas do utilizador. */
    @Query("SELECT COALESCE(AVG(t.impactScore), 0.0) FROM TaskLog t JOIN t.dailyEntry e WHERE e.appUser = :user")
    double avgImpactScoreByUser(@Param("user") AppUser user);

    /** Contagem total de tarefas do utilizador. */
    @Query("SELECT COUNT(t) FROM TaskLog t JOIN t.dailyEntry e WHERE e.appUser = :user")
    long countByUser(@Param("user") AppUser user);

    /** Contagem de tarefas de alto impacto (score >= 4) do utilizador. */
    @Query("SELECT COUNT(t) FROM TaskLog t JOIN t.dailyEntry e WHERE e.appUser = :user AND t.impactScore >= 4")
    long countHighImpactByUser(@Param("user") AppUser user);

    /** Tags usadas pelo utilizador com contagem de ocorrências, ordenadas por popularidade. */
    @Query("SELECT t.name, COUNT(DISTINCT tl.id) FROM TaskLog tl JOIN tl.tags t JOIN tl.dailyEntry de WHERE de.appUser = :user GROUP BY t.name ORDER BY COUNT(DISTINCT tl.id) DESC")
    List<Object[]> findTagsWithCountByUser(@Param("user") AppUser user);
}

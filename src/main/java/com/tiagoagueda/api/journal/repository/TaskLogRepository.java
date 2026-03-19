package com.tiagoagueda.api.journal.repository;

import com.tiagoagueda.api.journal.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
/**
 * Repositório de acesso a dados para TaskLog.
 *
 * Mantém operações CRUD e pode crescer para queries analíticas por período,
 * tags e produtividade.
 */
public interface TaskLogRepository extends JpaRepository<TaskLog, UUID> {
}
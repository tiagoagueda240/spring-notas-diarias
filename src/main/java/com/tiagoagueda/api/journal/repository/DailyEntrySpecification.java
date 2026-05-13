package com.tiagoagueda.api.journal.repository;

import com.tiagoagueda.api.journal.entity.DailyEntry;
import com.tiagoagueda.api.journal.entity.Tag;
import com.tiagoagueda.api.journal.entity.TaskLog;
import com.tiagoagueda.api.user.AppUser;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Especificações JPA para pesquisa e filtragem de entradas de diário.
 * Todas as condições são opcionais e combinadas com AND.
 */
public final class DailyEntrySpecification {

    private DailyEntrySpecification() {}

    /**
     * Constrói uma Specification combinada com todos os filtros fornecidos.
     *
     * @param user     utilizador proprietário das entradas (obrigatório)
     * @param q        pesquisa de texto livre (rawText ou título de tarefa), case-insensitive
     * @param tags     lista de nomes de tags — a entrada deve ter pelo menos uma
     * @param minScore score mínimo de impacto (inclusive) de pelo menos uma tarefa
     * @param from     data de início (inclusive), null ignora
     * @param to       data de fim (inclusive), null ignora
     */
    public static Specification<DailyEntry> buildFilter(
            AppUser user,
            String q,
            List<String> tags,
            Integer minScore,
            LocalDate from,
            LocalDate to
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Sempre filtrar pelo utilizador autenticado
            predicates.add(cb.equal(root.get("appUser"), user));

            // Filtro de texto: rawText OU título de tarefa
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.toLowerCase() + "%";
                Predicate inRawText = cb.like(cb.lower(root.get("rawText")), pattern);

                Join<DailyEntry, TaskLog> taskJoin = root.join("tasks", JoinType.LEFT);
                Predicate inTaskTitle = cb.like(cb.lower(taskJoin.get("title")), pattern);

                predicates.add(cb.or(inRawText, inTaskTitle));
            }

            // Filtro por tags (pelo menos uma das tags fornecidas)
            if (tags != null && !tags.isEmpty()) {
                List<String> normalizedTags = tags.stream()
                        .map(t -> t.trim().toLowerCase())
                        .filter(t -> !t.isBlank())
                        .toList();
                if (!normalizedTags.isEmpty()) {
                    Join<DailyEntry, TaskLog> taskJoin = root.join("tasks", JoinType.LEFT);
                    Join<TaskLog, Tag> tagJoin = taskJoin.join("tags", JoinType.LEFT);
                    predicates.add(tagJoin.get("name").in(normalizedTags));
                }
            }

            // Filtro por score mínimo: pelo menos uma tarefa com impactScore >= minScore
            if (minScore != null) {
                Join<DailyEntry, TaskLog> taskJoin = root.join("tasks", JoinType.LEFT);
                predicates.add(cb.greaterThanOrEqualTo(taskJoin.get("impactScore"), minScore));
            }

            // Filtro por intervalo de datas
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("entryDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("entryDate"), to));
            }

            // Evitar duplicados causados por JOINs
            if (query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

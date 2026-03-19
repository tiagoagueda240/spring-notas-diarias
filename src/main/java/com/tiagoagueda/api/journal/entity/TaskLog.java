package com.tiagoagueda.api.journal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Entidade de tarefa estruturada associada a uma entrada de diário.
 */
public class TaskLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int impactScore;
    private String impactJustification;

    // Relação ManyToOne: Várias tarefas pertencem a 1 texto diário
    @ManyToOne
    @JoinColumn(name = "daily_entry_id", nullable = false)
    private DailyEntry dailyEntry;

    // Relação ManyToMany: Uma tarefa pode ter várias tags, e uma tag pode estar em várias tarefas
    @ManyToMany
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();
}
package com.tiagoagueda.api.journal.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_logs")
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
    private List<Tag> tags = new ArrayList<>();

    public TaskLog() {}

    // Getters e Setters (podes gerar no teu IDE)

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(int impactScore) {
        this.impactScore = impactScore;
    }

    public String getImpactJustification() {
        return impactJustification;
    }

    public void setImpactJustification(String impactJustification) {
        this.impactJustification = impactJustification;
    }

    public DailyEntry getDailyEntry() {
        return dailyEntry;
    }

    public void setDailyEntry(DailyEntry dailyEntry) {
        this.dailyEntry = dailyEntry;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
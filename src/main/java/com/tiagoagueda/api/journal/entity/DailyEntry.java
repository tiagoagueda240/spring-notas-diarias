package com.tiagoagueda.api.journal.entity;

import com.tiagoagueda.api.user.AppUser;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade que representa a tabela 'daily_entries' na Base de Dados.
 * Guarda o texto inserido pelo utilizador antes e depois do processamento da IA.
 */
@Entity // Diz ao Spring: "Esta classe é uma tabela na base de dados"
@Table(name = "daily_entries") // Opcional: Define o nome exato da tabela
public class DailyEntry {

    @Id // Diz que esta é a Chave Primária (Primary Key)
    @GeneratedValue(strategy = GenerationType.UUID) // Gera o UUID automaticamente
    private UUID id;


    @Column(nullable = false)
    private LocalDate entryDate; // A data a que o registo se refere

    // columnDefinition = "TEXT" permite gravar strings maiores do que o limite padrão do VARCHAR(255)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawText; // O teu texto livre (pode ser enorme, por isso usamos TEXT)

    @Column(nullable = false)
    private boolean aiProcessed = false; // Começa sempre a 'false' até a IA ler o texto

    // Relacionamento 1-para-Muitos.
    // CascadeType.ALL significa que se apagarmos o DailyEntry, as Tarefas dele também são apagadas.
    // FetchType.LAZY é uma boa prática de performance: só carrega as tarefas se as pedirmos explicitamente.
    @OneToMany(mappedBy = "dailyEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskLog> tasks = new ArrayList<>();

    // Relacionamento Muitos-para-1. Vários registos de diário pertencem a 1 utilizador.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    // --- CONSTRUTORES ---
    public DailyEntry() {} // Construtor obrigatório para o JPA/Hibernate

    public DailyEntry(LocalDate entryDate, String rawText) {
        this.entryDate = entryDate;
        this.rawText = rawText;
        this.aiProcessed = false;
    }

    // --- GETTERS E SETTERS ---
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public boolean isAiProcessed() {
        return aiProcessed;
    }

    public void setAiProcessed(boolean aiProcessed) {
        this.aiProcessed = aiProcessed;
    }

    public List<TaskLog> getTasks() {
        return tasks;
    }


    /**
     * Métodos auxiliares para gerir relações bidirecionais.
     * Ao adicionar uma tarefa à lista, garantimos que a tarefa também sabe a que diário pertence.
     */
    public void addTask(TaskLog task) {
        this.tasks.add(task);
        task.setDailyEntry(this);
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }
}

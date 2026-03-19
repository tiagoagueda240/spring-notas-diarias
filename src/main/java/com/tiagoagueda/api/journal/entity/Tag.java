package com.tiagoagueda.api.journal.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // A tag tem de ser única na base de dados
    @Column(nullable = false, unique = true)
    private String name;

    public Tag() {}

    public Tag(String name) {
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

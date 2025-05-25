package com.example.spring_boot.entity;

import java.util.UUID;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class GameResultEntity {
    
    @Id
    @GeneratedValue
    private UUID id;

    private String boardName;
    private int steps;

    @Lob
    private String generationsJson;

    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public String getBoardName() {
        return boardName;
    }

    public int getSteps() {
        return steps;
    } 

    public String getGenerationsJson() {
        return generationsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setGenerationsJson(String generationsJson) {
        this.generationsJson = generationsJson;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

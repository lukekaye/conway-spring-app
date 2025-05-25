package com.example.spring_boot.dto;

import java.util.List;

public class GameResponse {
    private List<GenerationState> generations;

    public GameResponse(List<GenerationState> generations) {
        this.generations = generations;
    }

    public List<GenerationState> getGenerations() {
        return generations;
    }

    public void setGenerations(List<GenerationState> generations) {
        this.generations = generations;
    }
}

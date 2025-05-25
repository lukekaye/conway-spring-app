package com.example.spring_boot.dto;

public class GenerationState {
    private int generation;
    private int[][] board;

    public GenerationState(int generation, int[][] board) {
        this.generation = generation;
        this.board = board;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }
}

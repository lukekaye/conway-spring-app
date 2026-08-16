package com.example.spring_boot.service;

public class GameOfLife {
    private int rows;
    private int cols;
    private int[][] board;

    public GameOfLife(int[][] initialState) {
        if (initialState == null) {
            throw new InvalidBoardException("Initial board state cannot be null.");
        }
        if (initialState.length == 0) {
            throw new InvalidBoardException("Initial board state cannot be empty.");
        }
        int expectedCols = initialState[0].length;
        for (int row = 0; row < initialState.length; row++) {
            if (initialState[row] == null) {
                throw new InvalidBoardException("Board must be rectangular: row " + row + " is null.");
            }
            if (initialState[row].length != expectedCols) {
                throw new InvalidBoardException(
                    "Board must be rectangular: row 0 has length " + expectedCols
                    + ", but row " + row + " has length " + initialState[row].length + "."
                );
            }
        }
        rows = initialState.length;
        cols = initialState[0].length;
        board = initialState;
    }

    public int[][] nextGeneration() {
        int[][] next = new int[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int liveNeighbours = countLiveNeighbours(row, col);

                if (board[row][col] == 1) {
                    next[row][col] = (liveNeighbours == 2 || liveNeighbours == 3) ? 1 : 0;
                } else {
                    next[row][col] = (liveNeighbours == 3) ? 1 : 0;
                }
            }
        }
        board = next;

        return board;
    }

    private int countLiveNeighbours(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int r = row + i;
                int c = col + j;
                if ((i != 0 || j != 0) && r >= 0 && r < rows && c >= 0 && c < cols) {
                    count += board[r][c];
                }
            }
        }
        return count;
    }
}

package com.example.spring_boot.service;

public class GameOfLife {
    private int rows;
    private int cols;
    private int[][] board;

    public GameOfLife(int[][] initialState) {
        validateBoard(initialState);

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

    private void validateBoard(int[][] board) {
        if (board == null) {
            throw new InvalidBoardException("Initial board state cannot be null.");
        }
        if (board.length == 0) {
            throw new InvalidBoardException("Initial board state cannot be empty.");
        }
        if (board[0] == null) {
            throw new InvalidBoardException("Board must be rectangular: row 0 is null.");
        }
        int expectedCols = board[0].length;
        for (int row = 0; row < board.length; row++) {
            if (board[row] == null) {
                throw new InvalidBoardException("Board must be rectangular: row " + row + " is null.");
            }
            if (board[row].length != expectedCols) {
                throw new InvalidBoardException(
                    "Board must be rectangular: row 0 has length " + expectedCols
                    + ", but row " + row + " has length " + board[row].length + "."
                );
            }
            for (int col = 0; col < board[row].length; col++) {
                int value = board[row][col];
                if (value != 0 && value != 1) {
                    throw new InvalidBoardException(
                        "Board cells must contain only 0 or 1: row "
                        + row + ", column " + col + " has value " + value + "."
                    );
                }
            }
        }
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

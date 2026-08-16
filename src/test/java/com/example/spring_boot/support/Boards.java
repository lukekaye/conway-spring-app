package com.example.spring_boot.support;

/**
 * Builds and renders boards for tests, so no test carries a raw {@code int[][]}
 * literal. A failed assertion on a rendered board is legible; a failed
 * assertion on a 17x17 array of 0s and 1s is not.
 */
public final class Boards {

    private Boards() {
    }

    /**
     * Parses rows of {@code .} (dead) and {@code #} (alive) into a board.
     * Example: {@code parse(".#.", ".#.", ".#.")} is a vertical blinker.
     */
    public static int[][] parse(String... rows) {
        int[][] board = new int[rows.length][];
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            board[r] = new int[row.length()];
            for (int c = 0; c < row.length(); c++) {
                board[r][c] = row.charAt(c) == '#' ? 1 : 0;
            }
        }
        return board;
    }

    public static int[][] empty(int rows, int cols) {
        return new int[rows][cols];
    }

    /**
     * The standard glider, with enough dead space around it that it can
     * translate by (+1, +1) every 4 generations without touching the edge
     * of the board.
     */
    public static int[][] glider() {
        return parse(
                ".#......",
                "..#.....",
                "###.....",
                "........",
                "........",
                "........",
                "........",
                "........"
        );
    }

    /** Renders a board back to {@code .}/{@code #} rows for assertion messages. */
    public static String render(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell == 1 ? '#' : '.');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

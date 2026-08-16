package com.example.spring_boot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * These are the preconditions every other test in the suite assumes about the
 * app's built-in boards, checked once here rather than re-checked everywhere
 * they are used.
 */
class DefaultBoardsTest {

    static Stream<Arguments> boards() {
        return Stream.of(
                arguments("BLINKER", DefaultBoards.BLINKER),
                arguments("BLOCK", DefaultBoards.BLOCK),
                arguments("PULSAR", DefaultBoards.PULSAR)
        );
    }

    @ParameterizedTest(name = "{0} is rectangular, non-empty, and holds only 0 or 1")
    @MethodSource("boards")
    void isWellFormed(String name, int[][] board) {
        assertThat(board).isNotEmpty();

        int expectedCols = board[0].length;
        for (int[] row : board) {
            assertThat(row).hasSize(expectedCols);
            for (int cell : row) {
                assertThat(cell).isIn(0, 1);
            }
        }
    }
}

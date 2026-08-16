package com.example.spring_boot.service;

import static com.example.spring_boot.support.Boards.empty;
import static com.example.spring_boot.support.Boards.glider;
import static com.example.spring_boot.support.Boards.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link GameOfLife} is a plain class with the app's only real business
 * logic, so it carries the deepest coverage in this suite. Every test here
 * runs with no Spring context.
 */
class GameOfLifeTest {

    @Nested
    @DisplayName("the four rules of B3/S23, exhaustively over every neighbour count")
    class Rules {

        @ParameterizedTest(name = "a live cell with {0} live neighbours -> alive next generation = {1}")
        @CsvSource({
                "0, false", // underpopulation
                "1, false", // underpopulation
                "2, true",  // survival
                "3, true",  // survival
                "4, false", // overpopulation
                "5, false", // overpopulation
                "6, false", // overpopulation
                "7, false", // overpopulation
                "8, false", // overpopulation
        })
        void liveCellSurvivesOnlyOnTwoOrThreeNeighbours(int liveNeighbours, boolean expectedAlive) {
            int[][] board = centreWithNeighbours(1, liveNeighbours);

            int[][] next = new GameOfLife(board).nextGeneration();

            assertThat(next[1][1]).isEqualTo(expectedAlive ? 1 : 0);
        }

        @ParameterizedTest(name = "a dead cell with {0} live neighbours -> alive next generation = {1}")
        @CsvSource({
                "0, false",
                "1, false",
                "2, false",
                "3, true", // reproduction
                "4, false",
                "5, false",
                "6, false",
                "7, false",
                "8, false",
        })
        void deadCellComesAliveOnlyOnThreeNeighbours(int liveNeighbours, boolean expectedAlive) {
            int[][] board = centreWithNeighbours(0, liveNeighbours);

            int[][] next = new GameOfLife(board).nextGeneration();

            assertThat(next[1][1]).isEqualTo(expectedAlive ? 1 : 0);
        }

        /**
         * A 3x3 board where the centre cell (1,1) has all 8 of its neighbours
         * in bounds, so a given neighbour count is exact and unambiguous. The
         * first {@code liveNeighbours} of these 8 fixed positions are set
         * alive; which ones does not matter, only how many.
         */
        private int[][] centreWithNeighbours(int centre, int liveNeighbours) {
            int[][] positions = {{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 2}, {2, 0}, {2, 1}, {2, 2}};
            int[][] board = new int[3][3];
            board[1][1] = centre;
            for (int i = 0; i < liveNeighbours; i++) {
                board[positions[i][0]][positions[i][1]] = 1;
            }
            return board;
        }
    }

    @Nested
    @DisplayName("known patterns")
    class Patterns {

        @Test
        @DisplayName("a block is a still life")
        void blockIsStillLife() {
            int[][] block = parse("##", "##");

            int[][] next = new GameOfLife(block).nextGeneration();

            assertThat(next).isEqualTo(block);
        }

        @Test
        @DisplayName("a blinker oscillates between vertical and horizontal with period 2")
        void blinkerOscillatesWithPeriodTwo() {
            int[][] vertical = parse(".#.", ".#.", ".#.");
            int[][] horizontal = parse("...", "###", "...");
            GameOfLife game = new GameOfLife(vertical);

            assertThat(game.nextGeneration()).isEqualTo(horizontal);
            assertThat(game.nextGeneration()).isEqualTo(vertical);
        }

        @Test
        @DisplayName("a pulsar returns to its start state after 3 generations")
        void pulsarOscillatesWithPeriodThree() {
            GameOfLife game = new GameOfLife(DefaultBoards.PULSAR);

            game.nextGeneration();
            game.nextGeneration();
            int[][] afterThree = game.nextGeneration();

            assertThat(afterThree).isEqualTo(DefaultBoards.PULSAR);
        }

        @Test
        @DisplayName("a glider translates by (+1, +1) after 4 generations, keeping its shape")
        void gliderTranslatesDiagonallyEveryFourGenerations() {
            GameOfLife game = new GameOfLife(glider());
            int[][] expectedAfterFour = parse(
                    "........",
                    "..#.....",
                    "...#....",
                    ".###....",
                    "........",
                    "........",
                    "........",
                    "........"
            );

            int[][] afterFour = null;
            for (int i = 0; i < 4; i++) {
                afterFour = game.nextGeneration();
            }

            assertThat(afterFour).isEqualTo(expectedAfterFour);
        }
    }

    @Nested
    @DisplayName("board boundaries: no wraparound")
    class Boundaries {

        @Test
        @DisplayName("in a fully live 3x3 board, only the 4 corners survive")
        void onlyCornersSurviveOnAFullyLiveBoard() {
            // A corner has 3 in-bounds neighbours (survives). Every other cell
            // has 5 or 8 in-bounds neighbours (overpopulation, dies). On a
            // torus every cell here would have exactly 8 neighbours instead.
            int[][] fullyLive = parse("###", "###", "###");
            int[][] expected = parse("#.#", "...", "#.#");

            int[][] next = new GameOfLife(fullyLive).nextGeneration();

            assertThat(next).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("board shapes")
    class Shapes {

        @Test
        @DisplayName("a 1x1 board's only cell always dies, since it can never have a neighbour")
        void singleCellBoardAlwaysDies() {
            int[][] next = new GameOfLife(new int[][]{{1}}).nextGeneration();

            assertThat(next[0][0]).isEqualTo(0);
        }

        @Test
        @DisplayName("a non-square 2x3 board evolves without error")
        void wideNonSquareBoardEvolves() {
            int[][] next = new GameOfLife(parse("###", "...")).nextGeneration();

            assertThat(next).hasDimensions(2, 3);
        }

        @Test
        @DisplayName("a non-square 3x2 board evolves without error")
        void tallNonSquareBoardEvolves() {
            int[][] next = new GameOfLife(parse("##", "..", "..")).nextGeneration();

            assertThat(next).hasDimensions(3, 2);
        }

        @Test
        @DisplayName("an all-dead board stays all-dead")
        void allDeadBoardStaysAllDead() {
            int[][] next = new GameOfLife(empty(4, 4)).nextGeneration();

            assertThat(next).isEqualTo(empty(4, 4));
        }
    }

    @Nested
    @DisplayName("state and aliasing")
    class State {

        @Test
        @DisplayName("each call to nextGeneration advances the board by one generation")
        void advancesOneGenerationPerCall() {
            GameOfLife game = new GameOfLife(parse(".#.", ".#.", ".#."));

            int[][] afterOne = game.nextGeneration();
            int[][] afterTwo = game.nextGeneration();

            assertThat(afterOne).isNotEqualTo(afterTwo);
        }

        @Test
        @DisplayName("nextGeneration returns a live reference to the board, not a defensive copy")
        void returnedArrayIsLiveNotACopy() {
            GameOfLife game = new GameOfLife(new int[3][3]); // all dead
            int[][] first = game.nextGeneration(); // still all dead

            // Mutate the array GameOfLife handed back, exactly as a careless caller might.
            first[0][0] = 1;
            first[0][1] = 1;
            first[1][0] = 1;

            int[][] second = game.nextGeneration();

            // (1,1) now has exactly the 3 live neighbours just written above.
            // If it comes alive, the mutation reached GameOfLife's internal
            // state: nextGeneration() does not defend against this, so a
            // caller must copy the array itself for a stable snapshot — which
            // is exactly why GameController.deepCopy exists.
            assertThat(second[1][1]).isEqualTo(1);
        }

        @Test
        @DisplayName("nextGeneration does not modify the caller's original board array")
        void doesNotModifyCallersOriginalArray() {
            int[][] original = parse(".#.", ".#.", ".#.");
            int[][] snapshotBeforeCall = parse(".#.", ".#.", ".#.");

            new GameOfLife(original).nextGeneration();

            assertThat(original).isEqualTo(snapshotBeforeCall);
        }
    }

    @Test
    @DisplayName("a null initial board is rejected")
    void nullBoardThrows() {
        assertThatThrownBy(() -> new GameOfLife(null))
                .isInstanceOf(InvalidBoardException.class)
                .hasMessage("Initial board state cannot be null.");
    }

    @Nested
    @DisplayName("board cell values")
    class CellValues {

        @ParameterizedTest
        @CsvSource({
            "0",
            "1"
        })
        @DisplayName("0 and 1 are valid cell values")
        void binaryCellValuesAreAccepted(int value) {
            int[][] board = {
                {0, value},
                {1, 0}
            };

            assertThatCode(() -> new GameOfLife(board))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @CsvSource({
            "-1",
            "2",
            "99"
        })
        @DisplayName("a cell holding a value other than 0 or 1 is rejected")
        void nonBinaryCellValuesAreRejected(int invalidValue) {
            int[][] board = {
                {0, invalidValue},
                {1, 0}
            };

            assertThatThrownBy(() -> new GameOfLife(board))
                    .isInstanceOf(InvalidBoardException.class)
                    .hasMessage("Board cells must contain only 0 or 1: row 0, column 1 has value " + invalidValue + ".");
        }

        @Test
        @DisplayName("an invalid value anywhere in the board is rejected")
        void invalidValueInLaterRowIsRejected() {
            int[][] board = {
                {0, 1, 0},
                {0, 0, 0},
                {0, 0, -1}
            };

            assertThatThrownBy(() -> new GameOfLife(board))
                    .isInstanceOf(InvalidBoardException.class);
        }
    }

    @Nested
    @DisplayName("malformed boards are rejected with InvalidBoardException")
    class MalformedBoards {

        @Test
        @DisplayName("an empty board is rejected")
        void emptyBoardIsRejected() {
            assertThatThrownBy(() -> new GameOfLife(new int[0][]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Initial board state cannot be empty.");
        }

        @Test
        @DisplayName("a row shorter than row 0 is rejected")
        void shorterRowIsRejected() {
            int[][] jagged = {
                {0, 0, 0},
                {0, 0},
            };

            assertThatThrownBy(() -> new GameOfLife(jagged))
                    .isInstanceOf(InvalidBoardException.class)
                    .hasMessage("Board must be rectangular: row 0 has length 3, but row 1 has length 2.");
        }

        @Test
        @DisplayName("a row longer than row 0 is rejected")
        void longerRowIsRejected() {
            int[][] jagged = {
                    {0, 0},
                    {0, 0, 0},
            };

            assertThatThrownBy(() -> new GameOfLife(jagged))
                    .isInstanceOf(InvalidBoardException.class)
                    .hasMessage("Board must be rectangular: row 0 has length 2, but row 1 has length 3.");
        }

        @Test
        @DisplayName("a null row is rejected")
        void nullRowIsRejected() {
            int[][] board = new int[2][];
            board[0] = new int[]{0, 0};
            board[1] = null;

            assertThatThrownBy(() -> new GameOfLife(board))
                    .isInstanceOf(InvalidBoardException.class)
                    .hasMessage("Board must be rectangular: row 1 is null.");
        }
    }
}

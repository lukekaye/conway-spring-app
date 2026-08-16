package com.example.spring_boot.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.spring_boot.dto.GameRequest;
import com.example.spring_boot.entity.GameResultEntity;
import com.example.spring_boot.repository.GameResultRepository;
import com.example.spring_boot.support.Boards;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Loads only the web layer: no JPA, no database. {@link GameResultRepository}
 * is mocked, so this class also stands as the specification of the
 * controller's persistence contract.
 *
 * <p>Not covered: the {@code JsonProcessingException} branch in
 * {@code getNextGenerations}. The {@code ObjectMapper} it uses is constructed
 * inline in the method rather than injected, so nothing on this classpath can
 * make it fail. See defect 8.
 */
@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameResultRepository gameResultRepository;

    private static GameRequest request(int[][] board, String boardName, Integer steps) {
        GameRequest request = new GameRequest();
        request.setBoard(board);
        request.setBoardName(boardName);
        if (steps != null) {
            request.setSteps(steps);
        }
        return request;
    }

    /**
     * JsonPath's default JSON provider parses a JSON array into a
     * {@code java.util.List}, so a raw {@code int[][]} literal never equals
     * what {@code jsonPath(...).value(...)} reads back from the response.
     * Converting through the app's own {@link ObjectMapper} produces the same
     * list-of-lists shape the parsed JSON has.
     */
    private Object boardValue(int[][] board) {
        return objectMapper.convertValue(board, Object.class);
    }

    @Test
    @DisplayName("an explicit board runs for the given number of steps, generation 0 is the input")
    void explicitBoardRunsForGivenSteps() throws Exception {
        int[][] blinker = Boards.parse(".#.", ".#.", ".#.");

        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(blinker, null, 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations.length()").value(2))
                .andExpect(jsonPath("$.generations[0].generation").value(0))
                .andExpect(jsonPath("$.generations[0].board").value(boardValue(new int[][]{{0, 1, 0}, {0, 1, 0}, {0, 1, 0}})))
                .andExpect(jsonPath("$.generations[1].generation").value(1))
                .andExpect(jsonPath("$.generations[1].board").value(boardValue(new int[][]{{0, 0, 0}, {1, 1, 1}, {0, 0, 0}})));
    }

    @Test
    @DisplayName("steps of 10 returns generations 0 through 10 inclusive, 11 entries")
    void stepsOfTenReturnsElevenGenerations() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(null, "BLOCK", 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations.length()").value(11))
                .andExpect(jsonPath("$.generations[10].generation").value(10));
    }

    @Test
    @DisplayName("steps omitted defaults to 1, so 2 generations are returned")
    void stepsOmittedDefaultsToOne() throws Exception {
        GameRequest request = new GameRequest();
        request.setBoardName("BLOCK");
        // steps intentionally left unset: GameRequest.steps defaults to 1.

        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations.length()").value(2));
    }

    @Test
    @DisplayName("steps of 0 returns only the initial generation, per the README")
    void stepsOfZeroReturnsOnlyGenerationZero() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(null, "BLOCK", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations.length()").value(1))
                .andExpect(jsonPath("$.generations[0].generation").value(0));
    }

    @Test
    @DisplayName("a negative steps value also returns only the initial generation")
    void negativeStepsReturnsOnlyGenerationZero() throws Exception {
        // next.ps1 in the repo sends steps: -1, so this is an exercised path.
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(null, "BLOCK", -1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations.length()").value(1));
    }

    @Test
    @DisplayName("boardName selects a named default board when no board is given")
    void boardNameSelectsNamedBoard() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(null, "BLOCK", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations[0].board").value(boardValue(new int[][]{{1, 1}, {1, 1}})));
    }

    @Test
    @DisplayName("boardName lookup is case-insensitive")
    void boardNameLookupIsCaseInsensitive() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(null, "block", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations[0].board").value(boardValue(new int[][]{{1, 1}, {1, 1}})));
    }

    @Test
    @DisplayName("an unknown boardName falls back to the configured default board")
    void unknownBoardNameFallsBackToDefault() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(null, "NONSENSE", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations[0].board").value(boardValue(new int[][]{{0, 1, 0}, {0, 1, 0}, {0, 1, 0}})));
    }

    @Test
    @DisplayName("an empty boardName falls back to the configured default board")
    void emptyBoardNameFallsBackToDefault() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(null, "", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations[0].board").value(boardValue(new int[][]{{0, 1, 0}, {0, 1, 0}, {0, 1, 0}})));
    }

    @Test
    @DisplayName("neither board nor boardName given falls back to the configured default board")
    void neitherBoardNorBoardNameFallsBackToDefault() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(null, null, 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations[0].board").value(boardValue(new int[][]{{0, 1, 0}, {0, 1, 0}, {0, 1, 0}})));
    }

    @Test
    @DisplayName("when both board and boardName are given, board wins, per the README")
    void explicitBoardWinsOverBoardName() throws Exception {
        int[][] block = Boards.parse("##", "##");

        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(block, "PULSAR", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generations[0].board").value(boardValue(new int[][]{{1, 1}, {1, 1}})));
    }

    @Nested
    @DisplayName("the game.default-board property")
    class ConfiguredDefaultBoard {

        @Autowired
        private GameController gameController;

        // @TestPropertySource on a @Nested class does not reliably win over
        // the property already set in src/test/resources/application.properties
        // here, so the @Value-injected field is set directly instead. This is
        // the standard fallback for testing a @Value field's effect without
        // spinning up a second application context.
        @Test
        @DisplayName("is used when no board or boardName is supplied")
        void usesConfiguredDefault() throws Exception {
            String original = (String) ReflectionTestUtils.getField(gameController, "defaultBoardName");
            ReflectionTestUtils.setField(gameController, "defaultBoardName", "BLOCK");
            try {
                mockMvc.perform(post("/game/next")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request(null, null, 0))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.generations[0].board").value(boardValue(new int[][]{{1, 1}, {1, 1}})));
            } finally {
                ReflectionTestUtils.setField(gameController, "defaultBoardName", original);
            }
        }
    }

    @Nested
    @DisplayName("persistence")
    class Persistence {

        @Test
        @DisplayName("save is called exactly once, with boardName, steps, and createdAt set")
        void savesResultExactlyOnce() throws Exception {
            mockMvc.perform(post("/game/next")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request(null, "BLOCK", 3))))
                    .andExpect(status().isOk());

            ArgumentCaptor<GameResultEntity> captor = ArgumentCaptor.forClass(GameResultEntity.class);
            verify(gameResultRepository).save(captor.capture());
            verifyNoMoreInteractions(gameResultRepository);

            GameResultEntity saved = captor.getValue();
            assertThat(saved.getBoardName()).isEqualTo("BLOCK");
            assertThat(saved.getSteps()).isEqualTo(3);
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getGenerationsJson()).isNotBlank();
        }

        @Test
        @DisplayName("boardName is persisted even when an explicit board was supplied, per the README")
        void boardNamePersistsEvenWhenBoardSupplied() throws Exception {
            int[][] block = Boards.parse("##", "##");

            mockMvc.perform(post("/game/next")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request(block, "PULSAR", 0))))
                    .andExpect(status().isOk());

            ArgumentCaptor<GameResultEntity> captor = ArgumentCaptor.forClass(GameResultEntity.class);
            verify(gameResultRepository).save(captor.capture());
            assertThat(captor.getValue().getBoardName()).isEqualTo("PULSAR");
        }

        @Test
        @DisplayName("steps is persisted verbatim, negatives included")
        void stepsPersistsVerbatimIncludingNegative() throws Exception {
            mockMvc.perform(post("/game/next")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request(null, "BLOCK", -1))))
                    .andExpect(status().isOk());

            ArgumentCaptor<GameResultEntity> captor = ArgumentCaptor.forClass(GameResultEntity.class);
            verify(gameResultRepository).save(captor.capture());
            assertThat(captor.getValue().getSteps()).isEqualTo(-1);
        }

        @Test
        @DisplayName("the persisted generationsJson holds the same generations as the response body")
        void persistedJsonMatchesResponseBody() throws Exception {
            MvcResult result = mockMvc.perform(post("/game/next")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request(null, "PULSAR", 3))))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
            int responseGenerationCount = responseBody.get("generations").size();

            ArgumentCaptor<GameResultEntity> captor = ArgumentCaptor.forClass(GameResultEntity.class);
            verify(gameResultRepository).save(captor.capture());
            JsonNode persisted = objectMapper.readTree(captor.getValue().getGenerationsJson());

            assertThat(persisted.size()).isEqualTo(responseGenerationCount);
        }
    }

    @Test
    @DisplayName("two identical requests return identical responses (guards the shared DefaultBoards arrays)")
    void identicalRequestsReturnIdenticalResponses() throws Exception {
        String body = objectMapper.writeValueAsString(request(null, "PULSAR", 3));

        MvcResult first = mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult second = mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(second.getResponse().getContentAsString())
                .isEqualTo(first.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("a repository failure propagates uncaught, after the simulation already ran")
    void repositoryFailurePropagatesUncaught() {
        // MockMvc invokes the servlet directly, in-process: it never goes
        // through a real servlet container, so it cannot exercise the
        // container's error-page mechanism that turns an uncaught exception
        // into a 500 response. Here the exception can only be observed by
        // catching it, wrapped, at the call site. The client-visible 500 for
        // this exact scenario is proven for real in GameApiIT, which runs a
        // full embedded server.
        doThrow(new RuntimeException("database unavailable")).when(gameResultRepository).save(any());

        assertThatThrownBy(() -> mockMvc.perform(post("/game/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request(null, "BLOCK", 1)))))
                .hasRootCauseMessage("database unavailable");
    }

    @Test
    @DisplayName("DEFECT 3: an empty board is accepted by the DTO but crashes the simulation")
    void emptyBoardCrashesTheSimulation() {
        assertThatThrownBy(() -> mockMvc.perform(post("/game/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"board\": []}")))
                .hasRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("malformed JSON is rejected with 400")
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not valid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a missing request body is rejected with 400")
    void missingBodyReturns400() throws Exception {
        mockMvc.perform(post("/game/next").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("an unsupported content type is rejected with 415")
    void unsupportedContentTypeReturns415() throws Exception {
        mockMvc.perform(post("/game/next")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }
}

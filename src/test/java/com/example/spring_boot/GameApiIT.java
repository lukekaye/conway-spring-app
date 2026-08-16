package com.example.spring_boot;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.spring_boot.dto.GameRequest;
import com.example.spring_boot.entity.GameResultEntity;
import com.example.spring_boot.repository.GameResultRepository;
import com.example.spring_boot.support.AbstractDatabaseIT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameApiIT extends AbstractDatabaseIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GameResultRepository gameResultRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("POST /game/next returns 200 and persists a matching row in MySQL")
    void postNextPersistsAMatchingRow() throws Exception {
        GameRequest request = new GameRequest();
        request.setBoardName("PULSAR");
        request.setSteps(3);
        long countBefore = gameResultRepository.count();

        // GameResponse has no no-argument constructor, so it is read back as
        // a JsonNode rather than deserialised to a type, the same way
        // GameControllerTest reads the response body.
        ResponseEntity<String> response = restTemplate.postForEntity(url("/game/next"), request, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("generations")).hasSize(4); // generations 0..3

        assertThat(gameResultRepository.count()).isEqualTo(countBefore + 1);
        GameResultEntity saved = gameResultRepository.findAll().stream()
                .filter(e -> "PULSAR".equals(e.getBoardName()) && e.getSteps() == 3)
                .reduce((first, second) -> second) // the most recently saved match
                .orElseThrow();

        JsonNode persisted = objectMapper.readTree(saved.getGenerationsJson());
        assertThat(persisted).hasSize(4);
    }

    @Test
    @DisplayName("an empty board reaches a real client as 400, with the validation message as the body")
    void emptyBoardReachesRealClientAs400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> body = new HttpEntity<>("{\"board\": []}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url("/game/next"), body, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Initial board state cannot be empty.");
    }

    @Test
    @DisplayName("a jagged board reaches a real client as 400, with the validation message as the body")
    void jaggedBoardReachesRealClientAs400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> body = new HttpEntity<>("{\"board\": [[0,0,0],[0,0]]}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url("/game/next"), body, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Board must be rectangular: row 0 has length 3, but row 1 has length 2.");
    }

    @Test
    @DisplayName("GET /actuator/health reports UP")
    void healthEndpointReportsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/actuator/health"), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}

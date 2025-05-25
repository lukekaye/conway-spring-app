package com.example.spring_boot.controller;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;
import java.time.Instant;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.spring_boot.dto.GameRequest;
import com.example.spring_boot.dto.GameResponse;
import com.example.spring_boot.dto.GenerationState;
import com.example.spring_boot.service.GameOfLife;
import com.example.spring_boot.service.DefaultBoards;
import com.example.spring_boot.entity.GameResultEntity;
import com.example.spring_boot.repository.GameResultRepository;

@RestController
@RequestMapping("/game")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Value("${game.default-board:BLINKER}")
    private String defaultBoardName;

    @Autowired
    private GameResultRepository gameResultRepository;

    @PostMapping("/next")
    public ResponseEntity<GameResponse> getNextGenerations(@RequestBody GameRequest request) {
        int[][] board = request.getBoard();
        if (board == null) {
            board = getDefaultBoard(request.getBoardName());
        }

        GameOfLife game = new GameOfLife(board);
        List<GenerationState> generations = new ArrayList<>();

        generations.add(new GenerationState(0, deepCopy(board)));
        for (int generation = 1; generation <= request.getSteps(); generation++) {
            int[][] next = game.nextGeneration();
            generations.add(new GenerationState(generation, deepCopy(next)));
        }

        String json = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(generations);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialise generations to JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        GameResultEntity entity = new GameResultEntity();
        entity.setBoardName(request.getBoardName());
        entity.setSteps(request.getSteps());
        entity.setGenerationsJson(json);
        entity.setCreatedAt(Instant.now());

        gameResultRepository.save(entity);

        return ResponseEntity.ok(new GameResponse(generations));
    }

    private int[][] getDefaultBoard(String requestedName) {
        if (requestedName != null) {
            try {
                String fieldName = requestedName.toUpperCase();
                Field field = DefaultBoards.class.getField(fieldName);
                return (int[][]) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.warn("Invalid boardName '{}', falling back to default '{}'", requestedName, defaultBoardName);
            }
        } else {
            logger.info("boardName not provided, using default '{}'", defaultBoardName);
        }

        return resolveBoardByName(defaultBoardName);
    }

    private int[][] resolveBoardByName(String name) {
        try {
            Field field = DefaultBoards.class.getField(name.toUpperCase());
            return (int[][]) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException("Configured default board is invalid: " + name);
        }
    }

    private int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}

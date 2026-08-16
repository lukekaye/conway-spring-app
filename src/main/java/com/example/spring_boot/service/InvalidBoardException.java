package com.example.spring_boot.service;

/**
 * Thrown by {@link GameOfLife} when a board violates a shape invariant: it
 * is null, empty, or not rectangular.
 */
public class InvalidBoardException extends IllegalArgumentException {
    
    public InvalidBoardException(String message) {
        super(message);
    }
}

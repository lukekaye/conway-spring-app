package com.example.spring_boot.service;

/**
 * Thrown by {@link GameOfLife} when a board violates a shape invariant: it
 * is null, empty, not rectangular or contains a value other than 0 or 1.
 */
public class InvalidBoardException extends IllegalArgumentException {
    
    public InvalidBoardException(String message) {
        super(message);
    }
}

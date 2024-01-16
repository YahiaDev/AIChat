package com.aichatapi.exception;

public class AIChatException extends RuntimeException {

    private final String message;

    public AIChatException(String message) {
        super(message);
        this.message = message;
    }
}

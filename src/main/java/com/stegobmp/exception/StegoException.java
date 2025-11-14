package com.stegobmp.exception;

public class StegoException extends RuntimeException {

    public StegoException(String message) {
        super(message);
    }

    public StegoException(String message, Throwable cause) {
        super(message, cause);
    }
}
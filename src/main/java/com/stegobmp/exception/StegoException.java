package com.stegobmp.exception;

public class StegoException extends RuntimeException {

    public StegoException(String message) {
        super(message);
    }

    public StegoException(String message, Throwable cause) {
        super(message, cause);
    }

    public static StegoException insufficientCapacity(int required, int available) {
        return new StegoException(String.format(
            "Capacidad insuficiente: se requieren %d bytes pero solo hay %d disponibles",
            required, available
        ));
    }

    public static StegoException invalidBmpFormat(String reason) {
        return new StegoException("Formato BMP inválido: " + reason);
    }
}
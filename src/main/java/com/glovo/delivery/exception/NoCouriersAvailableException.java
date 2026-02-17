package com.glovo.delivery.exception;

public class NoCouriersAvailableException extends RuntimeException {

    public NoCouriersAvailableException(String message) {
        super(message);
    }
}

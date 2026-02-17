package com.glovo.delivery.exception;

public class CourierNotFoundException extends RuntimeException {

    public CourierNotFoundException(String message) {
        super(message);
    }
}

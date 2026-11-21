package com.zeldev.delivery_service.exception;

public class NoCourierAvailableException extends RuntimeException{
    public NoCourierAvailableException(String message) {
        super(message);
    }
}

package com.zeldev.delivery_service.handler;

import com.zeldev.delivery_service.exception.DeliveryNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class DeliveryExceptionHandler {
    @ExceptionHandler(DeliveryNotFoundException.class)
    public ResponseEntity<String> handleDeliveryNotFound(DeliveryNotFoundException exception) {
        return ResponseEntity.status(NOT_FOUND).body(exception.getMessage());
    }
}

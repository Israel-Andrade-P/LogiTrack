package com.zeldev.order_service.handler;

import com.zeldev.order_service.exception.OrderNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handler(OrderNotFoundException exp) {
        return ResponseEntity.status(NOT_FOUND).body(ErrorResponse.builder().code(NOT_FOUND.value()).message(exp.getMessage()).build());
    }
}

package com.intern.assignment.config;

import com.intern.assignment.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({DeviceNotFoundException.class, ShelfNotFoundException.class, ShelfPositionNotFoundException.class})
    public ResponseEntity<Map<String,Object>> handleNotFoundExceptions(Exception exception) {
        Map<String,Object> response = new HashMap<>();
        response.put("message", exception.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "found nothing");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({DeviceCannotBeCreatedException.class, ShelfPositionCannotBeCreatedException.class, ShelfCannotBeCreatedException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequestExceptions(Exception exception) {
        Map<String,Object> response = new HashMap<>();
        response.put("message", exception.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "incorrect input");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

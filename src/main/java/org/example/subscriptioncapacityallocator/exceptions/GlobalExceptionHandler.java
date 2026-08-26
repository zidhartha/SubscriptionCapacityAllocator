package org.example.subscriptioncapacityallocator.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidAllocationInputException.class)
    public ResponseEntity<Map<String,String>> handleInvalidInput(InvalidAllocationInputException e){
        return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
    }

    @ExceptionHandler(AllocationDecisionNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleNotFound(AllocationDecisionNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error",e.getLocalizedMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
    }

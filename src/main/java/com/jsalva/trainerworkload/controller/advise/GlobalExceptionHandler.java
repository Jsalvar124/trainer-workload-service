package com.jsalva.trainerworkload.controller.advise;

import com.jsalva.trainerworkload.exception.TrainerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TrainerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTrainerNotFound(
            TrainerNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "TRAINER_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }
}

package com.jsalva.trainerworkload.exception;

public class TrainerNotFoundException extends RuntimeException {
    public TrainerNotFoundException(String username) {
        super("Trainer with username "+ username + " not found");
    }
}

package com.jsalva.trainerworkload.dto.request;

public record TrainerWorkloadQueryMessageDto(
        String username,
        Integer year,
        Integer month
        ) {
}

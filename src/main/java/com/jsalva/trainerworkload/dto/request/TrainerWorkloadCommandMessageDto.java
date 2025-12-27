package com.jsalva.trainerworkload.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainerWorkloadCommandMessageDto(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotNull(message = "isActive is required")
        Boolean isActive,

        @NotNull(message = "trainingDate is required")
        LocalDate trainingDate,

        @NotNull
        @Positive(message = "trainingDuration must be > 0")
        Integer trainingDuration
) {
}

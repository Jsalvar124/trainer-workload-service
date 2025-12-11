package com.jsalva.trainerworkload.dto.request;


//1. Trainer Username
//2. Trainer First Name
//3. Trainer Last Name
//4. IsActive
//5. Training date
//6. Training duration
//7. Action Type (ADD/DELETE)

import com.jsalva.trainerworkload.entity.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainerWorkloadRequestDto(
        @NotBlank(message = "Trainer username is required")
        String username,

        @NotBlank(message = "Trainer first name is required")
        String firstName,

        @NotBlank(message = "Trainer last name is required")
        String lastName,

        @NotNull(message = "Active status is required")
        Boolean isActive,

        @NotNull(message = "Training date is required")
        LocalDate trainingDate,

        @NotNull(message = "Training duration is required")
        @Positive(message = "Training duration must be positive")
        Integer trainingDuration,  // in minutes

        @NotNull(message = "Action type is required")
        ActionType actionType
) {



}

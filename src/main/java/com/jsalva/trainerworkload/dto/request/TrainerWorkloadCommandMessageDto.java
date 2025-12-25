package com.jsalva.trainerworkload.dto.request;

import java.time.LocalDate;

public record TrainerWorkloadCommandMessageDto(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        LocalDate trainingDate,
        Integer trainingDuration
) {
}

package com.jsalva.trainerworkload.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TrainerWorkloadResponseDto(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        List<YearSummaryDto> yearSummaryDtoList
) {
}

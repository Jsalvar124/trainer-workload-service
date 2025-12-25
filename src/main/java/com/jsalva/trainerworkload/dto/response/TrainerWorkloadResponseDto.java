package com.jsalva.trainerworkload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TrainerWorkloadResponseDto(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        @JsonProperty("years")
        List<YearSummaryDto> yearSummaryDtoList
) {
}

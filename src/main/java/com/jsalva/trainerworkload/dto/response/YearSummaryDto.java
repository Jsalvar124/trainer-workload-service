package com.jsalva.trainerworkload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record YearSummaryDto(
        Integer year,
        @JsonProperty("months")
        List<MonthSummaryDto> monthSummaryDtoList
) {
}

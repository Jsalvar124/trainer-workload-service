package com.jsalva.trainerworkload.dto.response;

import java.util.List;

public record YearSummaryDto(
        Integer year,
        List<MonthSummaryDto> monthSummaryDtoList
) {
}

package com.jsalva.trainerworkload.dto.response;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record
MonthSummaryDto(
        Integer month,  // 1 = January, 12 = December
        Integer totalWorkload
) {
}

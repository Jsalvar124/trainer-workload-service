package com.jsalva.trainerworkload.dto.response;


public record
MonthSummaryDto(
        Integer month,  // 1 = January, 12 = December
        Integer totalWorkload
) {
}

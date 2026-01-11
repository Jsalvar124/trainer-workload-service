package com.jsalva.trainerworkload.mapper;

import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import com.jsalva.trainerworkload.dto.response.MonthSummaryDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.dto.response.YearSummaryDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TrainerWorkloadMapper {

    /**
     * Map domain entity to response DTO with optional year/month filtering
     */
    public TrainerWorkloadResponseDto toDto(TrainerMonthlyWorkload trainerWorkload, Integer year, Integer month){

        List<TrainerMonthlyWorkload.YearSummary> filteredYears = trainerWorkload.getYears();
        // Filter years if year if present
        if(year != null){
            filteredYears = filteredYears.stream()
                    .filter(y -> y.getYear().equals(year))
                    .toList();
        }

        List<YearSummaryDto> yearSummaries = filteredYears.stream()
                .map(yearSummary -> {
                    // Filter months if specified
                    List<TrainerMonthlyWorkload.MonthSummary> months = yearSummary.getMonths();
                    if (month != null) {
                        months = months.stream()
                                .filter(m -> m.getMonth().equals(month))
                                .toList();
                    }

                    // Map months to DTOs
                    List<MonthSummaryDto> monthDtos = months.stream()
                            .map(m -> new MonthSummaryDto(m.getMonth(), m.getTotalWorkload()))
                            .sorted(Comparator.comparing(MonthSummaryDto::month))
                            .toList();

                    return new YearSummaryDto(yearSummary.getYear(), monthDtos);
                })
                .filter(y -> !y.monthSummaryDtoList().isEmpty()) // Remove years with no matching months
                .sorted(Comparator.comparing(YearSummaryDto::year))
                .toList();

        return new TrainerWorkloadResponseDto(
                trainerWorkload.getUsername(),
                trainerWorkload.getFirstName(),
                trainerWorkload.getLastName(),
                trainerWorkload.getIsActive(),
                yearSummaries
        );
    }

    /**
     * Map domain entity to response DTO
     */
    public TrainerWorkloadResponseDto toDto(TrainerMonthlyWorkload trainer) {
        return toDto(trainer, null, null);
    }
}

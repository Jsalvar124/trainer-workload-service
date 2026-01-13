package com.jsalva.trainerworkload.service.impl;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.dto.response.MonthSummaryDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.dto.response.YearSummaryDto;
import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import com.jsalva.trainerworkload.enums.ActionType;
import com.jsalva.trainerworkload.exception.TrainerNotFoundException;
import com.jsalva.trainerworkload.mapper.TrainerWorkloadMapper;
import com.jsalva.trainerworkload.repository.TrainerWorkloadRepository;
import com.jsalva.trainerworkload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.List;

@Service
@Validated
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;

    private final TrainerWorkloadMapper trainerWorkloadMapper;

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    public TrainerWorkloadServiceImpl(TrainerWorkloadRepository trainerWorkloadRepository, TrainerWorkloadMapper trainerWorkloadMapper) {
        this.trainerWorkloadRepository = trainerWorkloadRepository;
        this.trainerWorkloadMapper = trainerWorkloadMapper;
    }

    @Override
    @Transactional
    public void updateWorkload(TrainerWorkloadCommandMessageDto requestDto, ActionType actionType) {
        logger.debug("Processing workload for trainer: {}, action: {}", requestDto.username(), actionType);

        TrainerMonthlyWorkload trainerMonthlyWorkload = findOrCreateTrainerSummary(requestDto);

        switch (actionType){
            case ADD -> {
                logger.info("Attempting to Add workload to database");
                addWorkload(trainerMonthlyWorkload, requestDto);
            }
            case DELETE -> {
                logger.info("Attempting to Delete workload from database");
                deleteWorkload(trainerMonthlyWorkload, requestDto);
            } case null, default -> throw new IllegalArgumentException(
                    "Unsupported action: " + actionType
            );
        }
    }

    // HELPER METHODS

    private TrainerMonthlyWorkload findOrCreateTrainerSummary(TrainerWorkloadCommandMessageDto requestDto) {
        String username = requestDto.username();
        return trainerWorkloadRepository.findByUsername(username)
                .map(existing -> {
                    logger.debug("Trainer already exists: {}, using existing data", username);
                    return existing;  // no updates from new Trainer Workload Requests.
                })
                .orElseGet(() -> {
                    logger.info("Creating new trainer: {}", username);
                    TrainerMonthlyWorkload newTrainer = new TrainerMonthlyWorkload();
                    newTrainer.setUsername(username);
                    newTrainer.setFirstName(requestDto.firstName());
                    newTrainer.setLastName(requestDto.lastName());
                    newTrainer.setIsActive(requestDto.isActive());
                    return trainerWorkloadRepository.save(newTrainer);
                });
    }

    private void addWorkload(TrainerMonthlyWorkload trainerMonthlyWorkload, TrainerWorkloadCommandMessageDto requestDto){

        // Extract data from dto
        int year = requestDto.trainingDate().getYear();
        int month = requestDto.trainingDate().getMonthValue();
        int duration = requestDto.trainingDuration();

        // Find or Create YearSummary (Inside TrainerMonthly Workload)
        TrainerMonthlyWorkload.YearSummary yearSummary = trainerMonthlyWorkload.getYears().stream()
                .filter(y-> y.getYear().equals(year))
                .findFirst()
                .orElseGet( () -> {
                    logger.debug("Creating new Month Summary for year {}", year);
                    TrainerMonthlyWorkload.YearSummary newYear = new TrainerMonthlyWorkload.YearSummary();
                    newYear.setYear(year);
                    trainerMonthlyWorkload.getYears().add(newYear);
                    return newYear;
                    }
                );

        // Find or create MonthSummary (inside TrainerMonthlyWorkload)
        TrainerMonthlyWorkload.MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("Creating new Month Summary for month {}", month);
                    TrainerMonthlyWorkload.MonthSummary newMonth = new TrainerMonthlyWorkload.MonthSummary();
                    newMonth.setMonth(month);
                    newMonth.setTotalWorkload(0);
                    yearSummary.getMonths().add(newMonth); // get the list of monthSummaries and add the new one.
                    return newMonth;
                });

        // Update month workload total
        int currentWorkload = monthSummary.getTotalWorkload();
        monthSummary.setTotalWorkload(currentWorkload + duration);
        // Save in database
        trainerWorkloadRepository.save(trainerMonthlyWorkload);
        logger.debug("Updated workload for {}-{}: {} minutes",
                yearSummary.getYear(),
                monthSummary.getMonth(),
                monthSummary.getTotalWorkload()
        );
    }

    private void deleteWorkload(TrainerMonthlyWorkload trainerMonthlyWorkload, TrainerWorkloadCommandMessageDto requestDto){
        int year = requestDto.trainingDate().getYear();
        int month = requestDto.trainingDate().getMonthValue();
        int duration = requestDto.trainingDuration();

        // Check if there is any training data for the given year.
        TrainerMonthlyWorkload.YearSummary yearSummary = trainerMonthlyWorkload.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElse(null);

        if(yearSummary == null){
            logger.error("Workload for year {} not found - trainer {}", year, trainerMonthlyWorkload.getUsername());
            return;
        }

        // Check if there is any training data for the given month.
        TrainerMonthlyWorkload.MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElse(null);

        if(monthSummary == null){
            logger.warn("Workload for month {} on year {} not found - trainer {} ", month,  year, trainerMonthlyWorkload.getUsername());
            return;
        }
        // Check final workload is positive
        int finalWorkload = monthSummary.getTotalWorkload() - requestDto.trainingDuration();
        if(finalWorkload <= 0){
            yearSummary.getMonths().remove(monthSummary);
            logger.info("Removed month {} from year {} (workload became zero/negative)", month, year);
            // If year has no months left, remove year too
            if (yearSummary.getMonths().isEmpty()) {
                trainerMonthlyWorkload.getYears().remove(yearSummary);
                logger.info("Removed year {}, no months remaining", year);
            }
            // Save in database
            trainerWorkloadRepository.save(trainerMonthlyWorkload);
        } else {
            monthSummary.setTotalWorkload(finalWorkload);
            // Save in database
            trainerWorkloadRepository.save(trainerMonthlyWorkload);
            logger.debug("Reduced workload for {}-{}: {} minutes remaining",
                    yearSummary.getYear(),
                    monthSummary.getMonth(),
                    monthSummary.getTotalWorkload()
            );
        }
    }

    @Override
    public TrainerWorkloadResponseDto getTrainerWorkload(String username, Integer year, Integer month) {

        logger.debug("Retrieving workload for trainer: {} (year: {}, month: {})", username, year, month);

        // Validate: if month is provided, year must also be provided
        if (month != null && year == null) {
            throw new IllegalArgumentException("Year is required when filtering by month");
        }

        // Check if trainer exists
        TrainerMonthlyWorkload trainerWorkload = trainerWorkloadRepository
                .findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer with username "+username+" not found"));

        // Map to DTOs
        return trainerWorkloadMapper.toDto(trainerWorkload, year, month);
    }

    @Override
    public List<TrainerWorkloadResponseDto> searchTrainersByName(String firstName, String lastName) {
        List<TrainerMonthlyWorkload> workloads;
        if (firstName != null && lastName != null) {
            // Both provided - search by full name
            workloads = trainerWorkloadRepository.findByFirstNameAndLastName(firstName, lastName);
        } else if (firstName != null) {
            // Only firstName provided
            workloads = trainerWorkloadRepository.findByFirstName(firstName);
        } else {
            // Only lastName provided (not null validation on controller layer)
            workloads = trainerWorkloadRepository.findByLastName(lastName);
        }
        List<TrainerWorkloadResponseDto> response = workloads.stream()
                .map(w -> trainerWorkloadMapper.toDto(w))
                .toList();
        return response;
    }
}

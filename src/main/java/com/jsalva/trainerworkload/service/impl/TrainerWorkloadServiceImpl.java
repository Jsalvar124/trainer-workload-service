package com.jsalva.trainerworkload.service.impl;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.dto.response.MonthSummaryDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.dto.response.YearSummaryDto;
import com.jsalva.trainerworkload.domain.MonthlyWorkload;
import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import com.jsalva.trainerworkload.enums.ActionType;
import com.jsalva.trainerworkload.exception.TrainerNotFoundException;
import com.jsalva.trainerworkload.repository.MonthlyWorkloadRepository;
import com.jsalva.trainerworkload.repository.TrainerSummaryRepository;
import com.jsalva.trainerworkload.service.TrainerWorkloadService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerSummaryRepository trainerSummaryRepository;
    private final MonthlyWorkloadRepository monthlyWorkloadRepository;


    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    public TrainerWorkloadServiceImpl(TrainerSummaryRepository trainerSummaryRepository, MonthlyWorkloadRepository monthlyWorkloadRepository) {
        this.trainerSummaryRepository = trainerSummaryRepository;
        this.monthlyWorkloadRepository = monthlyWorkloadRepository;
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
        return trainerSummaryRepository.findByUsername(username)
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
                    newTrainer.setActive(requestDto.isActive());
                    return trainerSummaryRepository.save(newTrainer);
                });
    }

    private void addWorkload(TrainerMonthlyWorkload trainerMonthlyWorkload, TrainerWorkloadCommandMessageDto requestDto){

        // Check if there is any training data for the given month.
        Optional<MonthlyWorkload> result = monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(trainerMonthlyWorkload.getUsername(), requestDto.trainingDate().getYear(), requestDto.trainingDate().getMonthValue());
        if(result.isPresent()){
            // There is data for the given month
            MonthlyWorkload monthlyWorkload = result.get();
            monthlyWorkload.setTotalWorkload(monthlyWorkload.getTotalWorkload() + requestDto.trainingDuration()); // add new training duration in minutes
            monthlyWorkloadRepository.save(monthlyWorkload);
            logger.debug("Updated workload for {}-{}: {} minutes",
                    monthlyWorkload.getYear(),
                    monthlyWorkload.getMonth(),
                    monthlyWorkload.getTotalWorkload());
        } else{
            // the given month has no trainings. Create new register.
            MonthlyWorkload newWorkload = new MonthlyWorkload();
            newWorkload.setTrainerSummary(trainerMonthlyWorkload);
            newWorkload.setYear(requestDto.trainingDate().getYear());
            newWorkload.setMonth(requestDto.trainingDate().getMonthValue());
            newWorkload.setTotalWorkload(requestDto.trainingDuration());
            monthlyWorkloadRepository.save(newWorkload);
            logger.debug("Created new workload for {}-{}: {} minutes",
                    newWorkload.getYear(),
                    newWorkload.getMonth(),
                    newWorkload.getTotalWorkload());
        }
    }

    private void deleteWorkload(TrainerMonthlyWorkload trainerMonthlyWorkload, TrainerWorkloadCommandMessageDto requestDto){
        // Check if there is any training data for the given month.
        Optional<MonthlyWorkload> result = monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(trainerMonthlyWorkload.getUsername(), requestDto.trainingDate().getYear(), requestDto.trainingDate().getMonthValue());
        if(result.isPresent()){
            // There is data for the given month
            MonthlyWorkload monthlyWorkload = result.get();
            // Check final workload is positive
            int finalWorkload = monthlyWorkload.getTotalWorkload() - requestDto.trainingDuration(); // subtract training duration
            if (finalWorkload <= 0) {
                monthlyWorkloadRepository.delete(monthlyWorkload); // delete row if it reaches zero or negative
                logger.info("Workload for {} {}/{} removed (became zero or negative)",
                        trainerMonthlyWorkload.getUsername(),
                        monthlyWorkload.getYear(),
                        monthlyWorkload.getMonth());
                return;
            }
            monthlyWorkload.setTotalWorkload(finalWorkload); // update training duration in minutes
            monthlyWorkloadRepository.save(monthlyWorkload);
            logger.debug("Updated workload for {}-{}: {} minutes",
                    monthlyWorkload.getYear(),
                    monthlyWorkload.getMonth(),
                    monthlyWorkload.getTotalWorkload());
        } else{
            logger.warn("No existing workload found for {} - {}/{}",
                    trainerMonthlyWorkload.getUsername(),
                    requestDto.trainingDate().getYear(),
                    requestDto.trainingDate().getMonthValue());
            // nothing to remove
        }
    }



    @Override
    public TrainerWorkloadResponseDto getTrainerWorkload(String username, Integer year, Integer month) {

        logger.debug("Retrieving workload for trainer: {} (year: {}, month: {})", username, year, month);

        TrainerMonthlyWorkload trainer = trainerSummaryRepository
                .findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer with username "+username+" not found"));

        // Validate: if month is provided, year must also be provided
        if (month != null && year == null) {
            throw new IllegalArgumentException("Year is required when filtering by month");
        }
        List<MonthlyWorkload> workloads;
        if(year == null){
            workloads = monthlyWorkloadRepository.findByTrainerSummary_Username(username);
        } else if(month == null){
            workloads = monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYear(username, year);
        } else {
            workloads = monthlyWorkloadRepository
                    .findByTrainerSummary_UsernameAndYearAndMonth(username, year, month)
                    .map(List::of)
                    .orElse(List.of());
        }

        Map<Integer, List<MonthlyWorkload>> byYear =
                workloads.stream()
                        .collect(Collectors.groupingBy(MonthlyWorkload::getYear));

        List<YearSummaryDto> yearSummaries =
                byYear.entrySet().stream()
                        .map(entry -> new YearSummaryDto(
                                entry.getKey(),
                                entry.getValue().stream()
                                        .map(mw -> new MonthSummaryDto(
                                                mw.getMonth(),
                                                mw.getTotalWorkload()
                                        ))
                                        .sorted(Comparator.comparing(MonthSummaryDto::month))
                                        .toList()
                        ))
                        .sorted(Comparator.comparing(YearSummaryDto::year))
                        .toList();

        return new TrainerWorkloadResponseDto(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getActive(),
                yearSummaries
        );
    }
}

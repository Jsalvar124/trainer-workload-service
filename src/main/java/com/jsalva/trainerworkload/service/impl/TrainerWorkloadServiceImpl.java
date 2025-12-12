package com.jsalva.trainerworkload.service.impl;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadRequestDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.entity.ActionType;
import com.jsalva.trainerworkload.entity.MonthlyWorkload;
import com.jsalva.trainerworkload.entity.TrainerSummary;
import com.jsalva.trainerworkload.repository.MonthlyWorkloadRepository;
import com.jsalva.trainerworkload.repository.TrainerSummaryRepository;
import com.jsalva.trainerworkload.service.TrainerWorkloadService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerSummaryRepository trainerSummaryRepository;
    private final MonthlyWorkloadRepository monthlyWorkloadRepository;


    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    public TrainerWorkloadServiceImpl(TrainerSummaryRepository trainerSummaryRepository, MonthlyWorkloadRepository monthlyWorkloadRepository) {
        this.trainerSummaryRepository = trainerSummaryRepository;
        this.monthlyWorkloadRepository = monthlyWorkloadRepository;
    }

    @Override
    public void updateWorkload(TrainerWorkloadRequestDto requestDto) {
        logger.debug("Processing workload for trainer: {}, action: {}", requestDto.username(), requestDto.actionType());

        TrainerSummary trainerSummary = findOrCreateTrainerSummary(requestDto);

        switch (requestDto.actionType()){
            case ADD -> {
                logger.info("Attempting to Add workload to database");
                addWorkload(trainerSummary, requestDto);
            }
            case DELETE -> {
                logger.info("Attempting to Delete workload from database");
                deleteWorkload(trainerSummary, requestDto);
            } case null, default -> throw new IllegalArgumentException(
                    "Unsupported action: " + requestDto.actionType()
            );
        }

    }

    // HELPER METHODS

    private TrainerSummary findOrCreateTrainerSummary(TrainerWorkloadRequestDto requestDto) {
        String username = requestDto.username();
        return trainerSummaryRepository.findByUsername(username)
                .map(existing -> {
                    logger.debug("Trainer already exists: {}, using existing data", username);
                    return existing;  // no updates from new Trainer Workload Requests.
                })
                .orElseGet(() -> {
                    logger.info("Creating new trainer: {}", username);
                    TrainerSummary newTrainer = new TrainerSummary();
                    newTrainer.setUsername(username);
                    newTrainer.setFirstName(requestDto.firstName());
                    newTrainer.setLastName(requestDto.lastName());
                    newTrainer.setActive(requestDto.isActive());
                    return trainerSummaryRepository.save(newTrainer);
                });
    }

    private void addWorkload(TrainerSummary trainerSummary, TrainerWorkloadRequestDto requestDto){

        // Check if there is any training data for the given month.
        Optional<MonthlyWorkload> result = monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(trainerSummary.getUsername(), requestDto.trainingDate().getYear(), requestDto.trainingDate().getMonthValue());
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
            newWorkload.setTrainerSummary(trainerSummary);
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

    private void deleteWorkload(TrainerSummary trainerSummary, TrainerWorkloadRequestDto requestDto){
        // Check if there is any training data for the given month.
        Optional<MonthlyWorkload> result = monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(trainerSummary.getUsername(), requestDto.trainingDate().getYear(), requestDto.trainingDate().getMonthValue());
        if(result.isPresent()){
            // There is data for the given month
            MonthlyWorkload monthlyWorkload = result.get();
            // Check final workload is positive
            int finalWorkload = monthlyWorkload.getTotalWorkload() - requestDto.trainingDuration(); // subtract training duration
            if (finalWorkload <= 0) {
                monthlyWorkloadRepository.delete(monthlyWorkload); // delete row if it reaches zero or negative
                logger.info("Workload for {} {}/{} removed (became zero or negative)",
                        trainerSummary.getUsername(),
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
                    trainerSummary.getUsername(),
                    requestDto.trainingDate().getYear(),
                    requestDto.trainingDate().getMonthValue());
            // nothing to remove
        }
    }



    @Override
    public TrainerWorkloadResponseDto getTrainerWorkload(String username, Integer year, Integer month) {
        logger.debug("Retrieving workload for trainer: {} (year: {}, month: {})", username, year, month);

        Optional<MonthlyWorkload> result = monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(username,year,month);

        if(result.isEmpty()){
            logger.error("Result not found");
        }

        return null;
    }
}

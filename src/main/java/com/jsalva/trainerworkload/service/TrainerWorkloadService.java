package com.jsalva.trainerworkload.service;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.enums.ActionType;

import java.util.List;

public interface TrainerWorkloadService {
    /**
     * Process trainer workload (ADD or DELETE training)
     */
    void updateWorkload(TrainerWorkloadCommandMessageDto requestDto, ActionType actionType);

    /**
     * Get trainer's workload history with optional filtering
     * @param username the trainer's username
     * @param year optional year filter (null = all years)
     * @param month optional month filter (null = all months, requires year)
     * @return nested structure of years → months → duration
     */
    TrainerWorkloadResponseDto getTrainerWorkload(String username, Integer year, Integer month);

    /**
     * Search trainers workload by first name and/or lastname
     * @param firstName the trainer's first name (null = all trainers)
     * @param lastName optional year filter
     * @return nested structure of years → months → duration
     */
    List<TrainerWorkloadResponseDto> searchTrainersByName(String firstName, String lastName);
}

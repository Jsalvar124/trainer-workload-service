package com.jsalva.trainerworkload.service;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.enums.ActionType;

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

}

package com.jsalva.trainerworkload.service;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.dto.request.TrainerWorkloadQueryMessageDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.enums.ActionType;

public interface TrainerWorkloadService {
    /**
     * Process trainer workload (ADD or DELETE training)
     */
    void updateWorkload(TrainerWorkloadCommandMessageDto messageDto, ActionType actionType);

    /**
     * Get trainer's workload history with optional filtering (QUERY training)
     */
    TrainerWorkloadResponseDto getTrainerWorkload(TrainerWorkloadQueryMessageDto messageDto, ActionType actionType);

}

package com.jsalva.trainerworkload.controller;

import com.jsalva.trainerworkload.TrainerworkloadApplication;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workload")
public class WorkloadController {

    private final Logger logger = LoggerFactory.getLogger(WorkloadController.class);

    private final TrainerWorkloadService trainerWorkloadService;

    public WorkloadController(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponseDto> getTrainerWorkloadByUsername(
            @PathVariable String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month ){

        logger.info("Received workload query for trainer: {}, year: {}, month: {}",
                username, year, month);
        TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainerWorkload(username, year, month);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<TrainerWorkloadResponseDto>> searchTrainersByName(
            @RequestParam(name = "first-name", required = false) String firstName,
            @RequestParam(name = "last-name", required = false) String lastName ){
        logger.info("Received workload query for trainer with name: {} and lastname: {}", firstName, lastName);
        List<TrainerWorkloadResponseDto> response = trainerWorkloadService.searchTrainersByName(firstName, lastName);
        return ResponseEntity.ok(response);
    }

}

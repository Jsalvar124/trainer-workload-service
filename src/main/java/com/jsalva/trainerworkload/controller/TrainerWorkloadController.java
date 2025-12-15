package com.jsalva.trainerworkload.controller;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadRequestDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workload")
public class TrainerWorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadController.class);

    private final TrainerWorkloadService trainerWorkloadService;


    public TrainerWorkloadController(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @PostMapping
    public ResponseEntity<Void> updateTrainerWorkload(@Valid @RequestBody TrainerWorkloadRequestDto requestDto){
        trainerWorkloadService.updateWorkload(requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponseDto> getTrainerWorkload(
            @PathVariable String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month ){

        logger.info("Received workload query for trainer: {}, year: {}, month: {}",
                username, year, month);
        TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainerWorkload(username, year, month);
        return ResponseEntity.ok(response);
    }
}

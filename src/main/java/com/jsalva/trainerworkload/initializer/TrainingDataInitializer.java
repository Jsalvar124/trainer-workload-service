package com.jsalva.trainerworkload.initializer;

import com.jsalva.trainerworkload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.client.RestTemplate;

public class TrainingDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDataInitializer.class);

    private final TrainerWorkloadService workloadService;
    private final RestTemplate restTemplate;

    @Value("${app.data.sync-on-startup:true}")
    private boolean syncOnStartup;

    @Value("${main.service.url:http://localhost:8080}")
    private String mainServiceUrl;

    public TrainingDataInitializer(TrainerWorkloadService workloadService, RestTemplate restTemplate) {
        this.workloadService = workloadService;
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("========================================");
        logger.info("Training Data Initializer Started");
        logger.info("========================================");

        // Check if sync is enabled
        if (!syncOnStartup) {
            logger.info("Startup sync is disabled (app.data.sync-on-startup=false)");
            logger.info("Skipping data initialization.");
            return;
        }

        try {
            syncTrainingsFromMainService();

            logger.info("========================================");
            logger.info("Data synchronization completed successfully!");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("========================================");
            logger.error("Failed to synchronize data from Main Service");
            logger.error("Error: {}", e.getMessage());
            logger.error("========================================");
            logger.warn("Service will start with empty database.");
            logger.warn("Data will be populated as training events arrive from Main Service.");
            // Don't throw exception - allow service to start anyway
        }
    }

    private void syncTrainingsFromMainService() {

        String url = mainServiceUrl + "/api/v1/trainings";

        logger.info("Sending GET request to: {}", url);

        //TODO

    }
}

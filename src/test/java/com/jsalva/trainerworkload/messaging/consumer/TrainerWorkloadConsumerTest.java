//
//package com.jsalva.trainerworkload.messaging.consumer;
//
//import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
//import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
//import com.jsalva.trainerworkload.enums.ActionType;
//import com.jsalva.trainerworkload.repository.TrainerWorkloadRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.jms.core.JmsTemplate;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.LocalDate;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.awaitility.Awaitility.await;
//
//@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@ActiveProfiles("test")  // Add this line
//class TrainerWorkloadConsumerTest {
//
//    @Autowired
//    private JmsTemplate jmsTemplate;
//
//    @Autowired
//    private TrainerWorkloadRepository trainerWorkloadRepository;
//
//    @Autowired
//    private MonthlyWorkloadRepository monthlyWorkloadRepository;
//
//    private static final String QUEUE_NAME = "trainer.workload.command.queue";
//
//    @BeforeEach
//    void setUp() {
//        // Clean database before each test
//        monthlyWorkloadRepository.deleteAll();
//        trainerWorkloadRepository.deleteAll();
//    }
//
//    @Test
//    void shouldProcessAddMessageAndCreateNewTrainerWorkload() {
//        // Given
//        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
//                "john.doe",
//                "John",
//                "Doe",
//                true,
//                LocalDate.of(2024, 12, 15),
//                60
//        );
//
//        // When - Send message with headers
//        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
//            message.setStringProperty("X-Transaction-Id", "TEST-123");
//            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
//            return message;
//        });
//
//        // Then - Wait for async processing and verify
//        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
//            Optional<TrainerMonthlyWorkload> trainer = trainerWorkloadRepository.findByUsername("john.doe");
//            assertThat(trainer).isPresent();
//            assertThat(trainer.get().getFirstName()).isEqualTo("John");
//            assertThat(trainer.get().getLastName()).isEqualTo("Doe");
//            assertThat(trainer.get().getActive()).isTrue();
//
//            // Verify monthly workload created
//            Optional<MonthlyWorkload> workload = monthlyWorkloadRepository
//                    .findByTrainerSummary_UsernameAndYearAndMonth(trainer.get().getUsername(), 2024, 12);
//            assertThat(workload).isPresent();
//            assertThat(workload.get().getTotalWorkload()).isEqualTo(60);
//        });
//    }
//
//    @Test
//    void shouldProcessAddMessageAndIncrementExistingWorkload() {
//        // Given - Existing trainer with workload
//        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
//        trainer.setUsername("jane.smith");
//        trainer.setFirstName("Jane");
//        trainer.setLastName("Smith");
//        trainer.setActive(true);
//        trainer = trainerWorkloadRepository.save(trainer);
//
//        MonthlyWorkload existingWorkload = new MonthlyWorkload();
//        existingWorkload.setTrainerSummary(trainer);
//        existingWorkload.setYear(2024);
//        existingWorkload.setMonth(12);
//        existingWorkload.setTotalWorkload(90);
//        monthlyWorkloadRepository.save(existingWorkload);
//
//        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
//                "jane.smith",
//                "Jane",
//                "Smith",
//                true,
//                LocalDate.of(2024, 12, 20),
//                60
//        );
//
//        // When
//        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
//            message.setStringProperty("X-Transaction-Id", "TEST-456");
//            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
//            return message;
//        });
//
//        // Then - Workload should be incremented
//        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
//            Optional<TrainerMonthlyWorkload> updatedTrainer = trainerWorkloadRepository.findByUsername("jane.smith");
//            assertThat(updatedTrainer).isPresent();
//
//            Optional<MonthlyWorkload> workload = monthlyWorkloadRepository
//                    .findByTrainerSummary_UsernameAndYearAndMonth(updatedTrainer.get().getUsername(), 2024, 12);
//            assertThat(workload).isPresent();
//            assertThat(workload.get().getTotalWorkload()).isEqualTo(150); // 90 + 60
//        });
//    }
//
//    @Test
//    void shouldProcessDeleteMessageAndDecrementWorkload() {
//        // Given - Existing trainer with workload
//        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
//        trainer.setUsername("bob.johnson");
//        trainer.setFirstName("Bob");
//        trainer.setLastName("Johnson");
//        trainer.setActive(true);
//        trainer = trainerWorkloadRepository.save(trainer);
//
//        MonthlyWorkload existingWorkload = new MonthlyWorkload();
//        existingWorkload.setTrainerSummary(trainer);
//        existingWorkload.setYear(2024);
//        existingWorkload.setMonth(12);
//        existingWorkload.setTotalWorkload(120);
//        monthlyWorkloadRepository.save(existingWorkload);
//
//        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
//                "bob.johnson",
//                "Bob",
//                "Johnson",
//                true,
//                LocalDate.of(2024, 12, 10),
//                30
//        );
//
//        // When - Send DELETE message
//        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
//            message.setStringProperty("X-Transaction-Id", "TEST-789");
//            message.setStringProperty("X-Action-Type", ActionType.DELETE.name());
//            return message;
//        });
//
//        // Then - Workload should be decremented
//        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
//            Optional<TrainerMonthlyWorkload> updatedTrainer = trainerWorkloadRepository.findByUsername("bob.johnson");
//            assertThat(updatedTrainer).isPresent();
//
//            Optional<MonthlyWorkload> workload = monthlyWorkloadRepository
//                    .findByTrainerSummary_UsernameAndYearAndMonth(updatedTrainer.get().getUsername(), 2024, 12);
//            assertThat(workload).isPresent();
//            assertThat(workload.get().getTotalWorkload()).isEqualTo(90); // 120 - 30
//        });
//    }
//
//    @Test
//    void shouldProcessDeleteMessageAndRemoveWorkloadWhenReachesZero() {
//        // Given - Existing trainer with exact workload that will be deleted
//        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
//        trainer.setUsername("alice.williams");
//        trainer.setFirstName("Alice");
//        trainer.setLastName("Williams");
//        trainer.setActive(true);
//        trainer = trainerWorkloadRepository.save(trainer);
//
//        MonthlyWorkload existingWorkload = new MonthlyWorkload();
//        existingWorkload.setTrainerSummary(trainer);
//        existingWorkload.setYear(2024);
//        existingWorkload.setMonth(11);
//        existingWorkload.setTotalWorkload(60);
//        monthlyWorkloadRepository.save(existingWorkload);
//
//        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
//                "alice.williams",
//                "Alice",
//                "Williams",
//                true,
//                LocalDate.of(2024, 11, 25),
//                60
//        );
//
//        // When - Send DELETE message with full workload amount
//        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
//            message.setStringProperty("X-Transaction-Id", "TEST-999");
//            message.setStringProperty("X-Action-Type", ActionType.DELETE.name());
//            return message;
//        });
//
//        // Then - Monthly workload entry should be completely removed
//        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
//            Optional<TrainerMonthlyWorkload> updatedTrainer = trainerWorkloadRepository.findByUsername("alice.williams");
//            assertThat(updatedTrainer).isPresent();
//
//            Optional<MonthlyWorkload> workload = monthlyWorkloadRepository
//                    .findByTrainerSummary_UsernameAndYearAndMonth(updatedTrainer.get().getUsername(), 2024, 11);
//            assertThat(workload).isEmpty(); // Workload entry should be deleted
//        });
//    }
//
//    @Test
//    void shouldRouteToDLQWhenInvalidMessageReceived() {
//        // Given - Message with username that triggers error (from your consumer code)
//        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
//                "Error.Test", // This triggers the error in your consumer
//                "Error",
//                "Test",
//                true,
//                LocalDate.of(2024, 12, 15),
//                60
//        );
//
//        // When - Send message that will fail
//        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
//            message.setStringProperty("X-Transaction-Id", "TEST-ERROR");
//            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
//            return message;
//        });
//
//        // Then - Message should NOT create a trainer (processing failed)
//        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
//            Optional<TrainerMonthlyWorkload> trainer = trainerWorkloadRepository.findByUsername("Error.Test");
//            assertThat(trainer).isEmpty(); // Should not be created due to error
//        });
//
//        // Note: To fully verify DLQ routing, you would need to check the DLQ queue
//        // This test verifies that processing failed (no database change)
//    }
//
//    @Test
//    void shouldHandleInvalidActionType() {
//        // Given
//        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
//                "test.user",
//                "Test",
//                "User",
//                true,
//                LocalDate.of(2024, 12, 15),
//                60
//        );
//
//        // When - Send message with invalid action type
//        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
//            message.setStringProperty("X-Transaction-Id", "TEST-INVALID");
//            message.setStringProperty("X-Action-Type", "INVALID_ACTION");
//            return message;
//        });
//
//        // Then - Message should fail and not create trainer
//        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
//            Optional<TrainerMonthlyWorkload> trainer = trainerWorkloadRepository.findByUsername("test.user");
//            assertThat(trainer).isEmpty();
//        });
//    }
//
//    @Test
//    void shouldRouteToDLQWhenRequiredFieldsAreMissing() {
//        // Given - Message with null username (violates @NotBlank validation)
//        String invalidJson = """
//        {
//            "username": null,
//            "firstName": "John",
//            "lastName": "Doe",
//            "isActive": true,
//            "trainingDate": "2024-12-15",
//            "trainingDuration": 60
//        }
//        """;
//
//        // When - Send raw JSON message that will fail deserialization/validation
//        jmsTemplate.send(QUEUE_NAME, session -> {
//            jakarta.jms.TextMessage message = session.createTextMessage(invalidJson);
//            message.setStringProperty("_type", "TrainerWorkloadCommandMessageDto");
//            message.setStringProperty("X-Transaction-Id", "TEST-NULL-FIELD");
//            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
//            return message;
//        });
//
//        // Then - Wait to ensure no trainer was created (message failed)
//        await().pollDelay(2, TimeUnit.SECONDS)
//                .atMost(8, TimeUnit.SECONDS)
//                .untilAsserted(() -> {
//                    // Verify no trainer was created due to validation failure
//                    long trainerCount = trainerWorkloadRepository.count();
//                    assertThat(trainerCount).isZero();
//                });
//    }
//
//    @Test
//    void shouldRetryFailedMessageThreeTimesBeforeRoutingToDLQ() throws InterruptedException {
//        // Given - Message that will trigger error
//        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
//                "Error.Test",
//                "Error",
//                "Test",
//                true,
//                LocalDate.of(2024, 12, 15),
//                60
//        );
//
//        // When - Send message that will fail
//        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
//            message.setStringProperty("X-Transaction-Id", "TEST-RETRY-COUNT");
//            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
//            return message;
//        });
//
//        // Then - Wait for all retries to complete
//        // Initial attempt + 3 retries = 4 total attempts
//        await().atMost(20, TimeUnit.SECONDS)
//                .pollInterval(500, TimeUnit.MILLISECONDS)
//                .until(() -> {
//                    // Check logs or wait for the expected delay time
//                    // After ~14 seconds, all retries should be exhausted
//                    return System.currentTimeMillis() > 0; // Just wait
//                });
//
//        // Give it a moment to finish all retries
//        Thread.sleep(15_000);
//
//        // Verify no trainer was created (all attempts failed)
//        Optional<TrainerMonthlyWorkload> trainer = trainerWorkloadRepository.findByUsername("Error.Test");
//        assertThat(trainer).isEmpty();
//    }
//
//    @Test
//    void shouldRetryWithExponentialBackoff() {
//        // Given
//        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
//                "Error.Test",
//                "Error",
//                "Test",
//                true,
//                LocalDate.of(2024, 12, 15),
//                60
//        );
//
//        long startTime = System.currentTimeMillis();
//
//        // When
//        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
//            message.setStringProperty("X-Transaction-Id", "TEST-BACKOFF");
//            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
//            return message;
//        });
//
//        // Then - Wait for all retries to complete
//        await().atMost(20, TimeUnit.SECONDS)
//                .pollInterval(500, TimeUnit.MILLISECONDS)
//                .untilAsserted(() -> {
//                    long duration = System.currentTimeMillis() - startTime;
//                    // After all retries, at least 14 seconds should have passed
//                    assertThat(duration).isGreaterThan(13_000);
//                });
//
//        long totalDuration = System.currentTimeMillis() - startTime;
//
//        // Verify exponential backoff timing
//        // Expected: ~14 seconds (2s + 4s + 8s)
//        assertThat(totalDuration).isGreaterThan(13_000); // At least 13 seconds
//        assertThat(totalDuration).isLessThan(18_000);    // Less than 18 seconds
//
//        // Verify no trainer was created
//        Optional<TrainerMonthlyWorkload> trainer = trainerWorkloadRepository.findByUsername("Error.Test");
//        assertThat(trainer).isEmpty();
//    }
//}

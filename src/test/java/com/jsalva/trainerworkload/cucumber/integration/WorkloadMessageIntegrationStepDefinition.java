package com.jsalva.trainerworkload.cucumber.integration;

import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.enums.ActionType;
import com.jsalva.trainerworkload.repository.TrainerWorkloadRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.jms.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WorkloadMessageIntegrationStepDefinition {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    private TrainerMonthlyWorkload trainer;

    @Value("${jms.queue.trainer-workload:trainer.workload.command.queue}")
    private String QUEUE_NAME;

    @Value("${jms.queue.dead-letter:ActiveMQ.DLQ}")
    private String DLQ_NAME;

    // ========== SCENARIO 1: Existing month and trainer (ADD) ==========

    @When("a workload message is sent to the queue with the following details:")
    public void a_workload_message_is_sent_to_the_queue_with_the_following_details(Map<String, String> messageDetails) {
        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
                messageDetails.get("username"),
                messageDetails.get("firstName"),
                messageDetails.get("lastName"),
                Boolean.parseBoolean(messageDetails.get("isActive")),
                LocalDate.parse(messageDetails.get("date")),
                Integer.parseInt(messageDetails.get("duration"))
        );

        // When - Send message with headers
        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
            message.setStringProperty("X-Transaction-Id", "TEST-123");
            message.setStringProperty("X-Action-Type", messageDetails.get("actionType"));
            return message;
        });
    }

    @Then("a trainer {string} should exist in the database")
    public void a_trainer_should_exist_in_the_database(String username) {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<TrainerMonthlyWorkload> result = trainerWorkloadRepository.findByUsername(username);
            assertThat(result).isPresent();
            trainer = result.get();
            assertThat(trainer.getFirstName()).isEqualTo("Juan");
            assertThat(trainer.getLastName()).isEqualTo("Perez");
            assertThat(trainer.getIsActive()).isTrue();
        });
    }

    @Then("trainer {string} should have {int} hours in December {int}")
    public void trainer_should_have_hours_in_december(String username, Integer totalWorkload, Integer year) {
        // Verify year was created
        assertThat(trainer.getYears()).hasSize(1);
        TrainerMonthlyWorkload.YearSummary yearSummary = trainer.getYears().getFirst();
        assertThat(yearSummary.getYear()).isEqualTo(year);

        // Verify month was created with correct workload
        assertThat(yearSummary.getMonths()).hasSize(1);
        TrainerMonthlyWorkload.MonthSummary monthSummary = yearSummary.getMonths().getFirst();
        assertThat(monthSummary.getMonth()).isEqualTo(12);  // December
        assertThat(monthSummary.getTotalWorkload()).isEqualTo(totalWorkload);
    }


    @Given("a trainer {string} exists with {int} hours in December {int}")
    public void a_trainer_exists_with_hours_in_february(String username, Integer currentHours, Integer year) {
        TrainerMonthlyWorkload existingTrainer = new TrainerMonthlyWorkload();
        existingTrainer.setUsername(username);
        existingTrainer.setFirstName("Juan");
        existingTrainer.setLastName("Perez");
        existingTrainer.setIsActive(true);

        TrainerMonthlyWorkload.YearSummary yearSummary = new TrainerMonthlyWorkload.YearSummary();
        yearSummary.setYear(year);

        TrainerMonthlyWorkload.MonthSummary monthSummary = new TrainerMonthlyWorkload.MonthSummary();
        monthSummary.setMonth(12);
        monthSummary.setTotalWorkload(currentHours);
        yearSummary.getMonths().add(monthSummary);

        existingTrainer.getYears().add(yearSummary);
        // persist trainer
        trainerWorkloadRepository.save(existingTrainer);
    }


    @When("an invalid workload message is sent for {string}")
    public void an_invalid_workload_message_is_sent_for(String errorUsername) {
        TrainerWorkloadCommandMessageDto messageDto = new TrainerWorkloadCommandMessageDto(
                errorUsername,
                "Error",
                "Username",
                true,
                LocalDate.of(2024, 12, 15),
                60
        );

        jmsTemplate.convertAndSend(QUEUE_NAME, messageDto, message -> {
            message.setStringProperty("X-Transaction-Id", "TEST-ERROR");
            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
            return message;
        });
    }
    @Then("the trainer {string} should not be created")
    public void the_trainer_should_not_be_created(String errorUsername) {
        // Wait a bit to ensure processing attempt completed
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<TrainerMonthlyWorkload> trainer = trainerWorkloadRepository.findByUsername(errorUsername);
            assertThat(trainer).isEmpty();
        });
    }
    @Then("the message should be in the Dead Letter Queue")
    public void the_message_should_be_in_the_dead_letter_queue() {
        // Poll the DLQ to verify message is there
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Message dlqMessage = jmsTemplate.receive(DLQ_NAME);
            assertThat(dlqMessage).isNotNull();

            // Verify it's the error message
            String transactionId = dlqMessage.getStringProperty("X-Transaction-Id");
            assertThat(transactionId).isEqualTo("TEST-ERROR");
        });
    }

    @When("a message with missing username is sent to the queue")
    public void a_message_with_missing_username_is_sent_to_the_queue() {
        String invalidJson = """
    {
        "username": null,
        "firstName": "John",
        "lastName": "Doe",
        "isActive": true,
        "trainingDate": "2024-12-15",
        "trainingDuration": 60
    }
    """;

        jmsTemplate.send(QUEUE_NAME, session -> {
            jakarta.jms.TextMessage message = session.createTextMessage(invalidJson);
            message.setStringProperty("_type", "TrainerWorkloadCommandMessageDto");
            message.setStringProperty("X-Transaction-Id", "TEST-NULL-FIELD");
            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
            return message;
        });
    }
    @Then("no trainers should be created in the database")
    public void no_trainers_should_be_created_in_the_database() {
        // Wait to ensure processing attempt completed
        await().pollDelay(2, TimeUnit.SECONDS)
                .atMost(8, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    long trainerCount = trainerWorkloadRepository.count();
                    assertThat(trainerCount).isZero();
                });
    }

    @Then("the message with missing fields should be in the Dead Letter Queue")
    public void the_message_with_missing_fields_should_be_in_the_dead_letter_queue() {
        // Poll the DLQ to verify message is there
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Message dlqMessage = jmsTemplate.receive(DLQ_NAME);
            assertThat(dlqMessage).isNotNull();

            // Verify it's the error message
            String transactionId = dlqMessage.getStringProperty("X-Transaction-Id");
            assertThat(transactionId).isEqualTo("TEST-NULL-FIELD");
        });
    }
}

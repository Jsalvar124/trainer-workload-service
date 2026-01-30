package com.jsalva.trainerworkload.cucumber.integration;

import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.enums.ActionType;
import com.jsalva.trainerworkload.repository.TrainerWorkloadRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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
            message.setStringProperty("X-Action-Type", ActionType.ADD.name());
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
}

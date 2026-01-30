package com.jsalva.trainerworkload.cucumber;

import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.dto.response.MonthSummaryDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.dto.response.YearSummaryDto;
import com.jsalva.trainerworkload.enums.ActionType;
import com.jsalva.trainerworkload.exception.TrainerNotFoundException;
import com.jsalva.trainerworkload.repository.TrainerWorkloadRepository;
import com.jsalva.trainerworkload.service.TrainerWorkloadService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WorkloadServiceStepDefinition {
    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    @Autowired   // ← New annotation!
    private TrainerWorkloadRepository trainerWorkloadRepository;

    private TrainerMonthlyWorkload trainer;
    private TrainerMonthlyWorkload.MonthSummary monthSummary;
    private TrainerMonthlyWorkload.YearSummary yearSummary;
    private TrainerWorkloadCommandMessageDto commandDto;
    private TrainerWorkloadResponseDto responseDto;
    private Exception thrownException;

    // ========== SCENARIO 1: Existing month and trainer (ADD) ==========


    @Given("a trainer {string} exists with {int} hours in February {int}")
    public void a_trainer_exists_with_hours_in_february(String username, Integer currentHours, Integer year) {
        trainer = new TrainerMonthlyWorkload();
        trainer.setUsername(username);
        trainer.setFirstName("Juan");
        trainer.setLastName("Perez");
        trainer.setIsActive(true);

        yearSummary = new TrainerMonthlyWorkload.YearSummary();
        yearSummary.setYear(year);

        monthSummary = new TrainerMonthlyWorkload.MonthSummary();
        monthSummary.setMonth(2);
        monthSummary.setTotalWorkload(currentHours);
        yearSummary.getMonths().add(monthSummary);

        trainer.getYears().add(yearSummary);

        when(trainerWorkloadRepository.findByUsername(username))
                .thenReturn(Optional.of(trainer));
    }

    @When("I add {int} hours of workload for {string} on {string}")
    public void i_add_hours_of_workload_for_on(Integer hours, String username, String date) {
        commandDto = new TrainerWorkloadCommandMessageDto(
                username,
                "Juan",
                "Perez",
                true,
                LocalDate.parse(date),
                hours
        );

        trainerWorkloadService.updateWorkload(commandDto, ActionType.ADD);
    }
    @Then("the trainer should have {int} total hours in February {int}")
    public void the_trainer_should_have_total_hours_in_february(Integer totalHours, Integer year) {
        assertEquals(totalHours, monthSummary.getTotalWorkload());  // Same reference
        assertEquals(totalHours, trainer.getYears().getFirst().getMonths().getFirst().getTotalWorkload());
        verify(trainerWorkloadRepository).save(trainer);
    }

    // ========== SCENARIO 2: New Trainer ==========

    @Given("no trainer with username {string} exists")
    public void no_trainer_with_username_exists(String username) {
        commandDto = new TrainerWorkloadCommandMessageDto(
                username,
                "New",
                "Trainer",
                true,
                LocalDate.of(2026, 2, 13),
                100
        );

        when(trainerWorkloadRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        when(trainerWorkloadRepository.save(any(TrainerMonthlyWorkload.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @When("I add {int} hours of workload")
    public void i_add_hours_of_workload(Integer int1) {
        trainerWorkloadService.updateWorkload(commandDto, ActionType.ADD);
    }

    @Then("trainer {string} should be created")
    public void trainer_should_be_created(String username) {
        ArgumentCaptor<TrainerMonthlyWorkload> captor =
                ArgumentCaptor.forClass(TrainerMonthlyWorkload.class);

        verify(trainerWorkloadRepository, times(2)).save(captor.capture());
        // once for create, once for update

        trainer = captor.getValue(); // last save

        // Trainer info
        assertEquals(username, trainer.getUsername());
        assertEquals("New", trainer.getFirstName());
        assertEquals("Trainer", trainer.getLastName());
        assertTrue(trainer.getIsActive());

        // Year
        assertEquals(1, trainer.getYears().size());
        TrainerMonthlyWorkload.YearSummary yearSummary =
                trainer.getYears().getFirst();
        assertEquals(2026, yearSummary.getYear());

        // Month
        assertEquals(1, yearSummary.getMonths().size());
        TrainerMonthlyWorkload.MonthSummary monthSummary =
                yearSummary.getMonths().getFirst();
        assertEquals(2, monthSummary.getMonth());
        assertEquals(100, monthSummary.getTotalWorkload());
    }

    // ========== SCENARIO 3: Existing month and trainer (DELETE) ==========


    @When("I delete {int} hours of workload for {string} on {string}")
    public void i_delete_hours_of_workload_for_on(Integer hours, String username, String date) {
        commandDto = new TrainerWorkloadCommandMessageDto(
                username,
                "Juan",
                "Perez",
                true,
                LocalDate.parse(date),
                hours
        );

        trainerWorkloadService.updateWorkload(commandDto, ActionType.DELETE);
    }

    @Then("February {int} should still exist for trainer {string}")
    public void february_should_still_exist_for_trainer(Integer int1, String string) {
        assertEquals(1, trainer.getYears().size());  // Year still exists
    }
    @Then("year {int} should still exist for trainer {string}")
    public void year_should_still_exist_for_trainer(Integer int1, String string) {
        assertEquals(1, trainer.getYears().getFirst().getMonths().size());  // Month still exists
        verify(trainerWorkloadRepository).save(trainer);

    }

    // ========== SCENARIO 4: Existing month and trainer, all workload deleted (DELETE) ==========

    @Then("trainer {string} should have no months in {int}")
    public void trainer_should_have_no_months_in(String string, Integer int1) {
        assertTrue(yearSummary.getMonths().isEmpty(), "Month should be removed when workload becomes zero");
    }
    @Then("trainer {string} should have no years recorded")
    public void trainer_should_have_no_years_recorded(String string) {
        assertTrue(trainer.getYears().isEmpty(), "Year should be removed when no months remain");
        verify(trainerWorkloadRepository).save(trainer);
    }

    // ========== SCENARIO 5: Existing trainer, GET Workload, no filters ==========

    @When("I retrieve the workload for {string}")
    public void i_retrieve_the_workload_for(String username) {
        responseDto = trainerWorkloadService.getTrainerWorkload(username, null, null);
    }

    @Then("the response should contain trainer details:")
    public void the_response_should_contain_trainer_details(Map<String, String> expectedDetails) {
        assertEquals(expectedDetails.get("username"), responseDto.username());
        assertEquals(expectedDetails.get("firstName"), responseDto.firstName());
        assertEquals(expectedDetails.get("lastName"), responseDto.lastName());
        assertEquals(Boolean.parseBoolean(expectedDetails.get("isActive")), responseDto.isActive());
    }

    @Then("the response should contain {int} year")
    public void the_response_should_contain_year(Integer yearCount) {
        assertEquals(yearCount, responseDto.yearSummaryDtoList().size());

    }
    @Then("the response should contain year {int} with {int} month")
    public void the_response_should_contain_year_with_month(Integer year, Integer monthCount) {
        YearSummaryDto yearSummaryDto = responseDto.yearSummaryDtoList().getFirst();
        assertEquals(year, yearSummaryDto.year());

        assertEquals(monthCount, yearSummaryDto.monthSummaryDtoList().size());

        MonthSummaryDto monthSummaryDto = yearSummaryDto.monthSummaryDtoList().getFirst();
        assertEquals(2, monthSummaryDto.month());
    }
    @Then("the response should show {int} hours in February {int}")
    public void the_response_should_show_hours_in_february(Integer totalWorkload, Integer year) {
        assertEquals(totalWorkload, responseDto.yearSummaryDtoList().getFirst().monthSummaryDtoList().getFirst().totalWorkload());

    }

    // ========== SCENARIO 6: Non-Existing trainer, GET Workload fails ==========

    @When("I attempt to retrieve the workload for {string}")
    public void i_attempt_to_retrieve_the_workload_for(String username) {
        thrownException = assertThrows(TrainerNotFoundException.class, () -> {
            trainerWorkloadService.getTrainerWorkload(username, null, null);
        });
    }
    @Then("the operation should fail with {string}")
    public void the_operation_should_fail_with(String expectedMessage) {
        assertNotNull(thrownException);
        assertEquals(expectedMessage, thrownException.getMessage());
    }

}

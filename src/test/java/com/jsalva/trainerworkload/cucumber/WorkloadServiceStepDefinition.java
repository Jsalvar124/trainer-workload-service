package com.jsalva.trainerworkload.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class WorkloadServiceStepDefinition {

    @Given("a trainer {string} exists with {int} hours in February {int}")
    public void a_trainer_exists_with_hours_in_february(String string, Integer int1, Integer int2) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
    @When("I add {int} hours of workload for {string} on {string}")
    public void i_add_hours_of_workload_for_on(Integer int1, String string, String string2) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
    @Then("the trainer should have {int} total hours in February {int}")
    public void the_trainer_should_have_total_hours_in_february(Integer int1, Integer int2) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
}

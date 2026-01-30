Feature: Trainer workload Management
  Scenario: Add workload to existing trainer in existing month
    Given a trainer "Juan.Perez" exists with 100 hours in February 2026
    When I add 120 hours of workload for "Juan.Perez" on "2026-02-15"
    Then the trainer should have 220 total hours in February 2026

  Scenario: Add workload to new trainer
    Given no trainer with username "New.Trainer" exists
    When I add 100 hours of workload
    Then trainer "New.Trainer" should be created

  Scenario: Delete workload for an existing trainer in an existing month
    Given a trainer "Juan.Perez" exists with 220 hours in February 2026
    When I delete 100 hours of workload for "Juan.Perez" on "2026-02-10"
    Then the trainer should have 120 total hours in February 2026
    And February 2026 should still exist for trainer "Juan.Perez"
    And year 2026 should still exist for trainer "Juan.Perez"

  Scenario: Delete workload removes empty month and year
    Given a trainer "Juan.Perez" exists with 100 hours in February 2026
    When I delete 100 hours of workload for "Juan.Perez" on "2026-02-10"
    Then trainer "Juan.Perez" should have no months in 2026
    And trainer "Juan.Perez" should have no years recorded

  Scenario: Retrieve workload for existing trainer
    Given a trainer "Juan.Perez" exists with 100 hours in February 2026
    When I retrieve the workload for "Juan.Perez"
    Then the response should contain trainer details:
      | username  | Juan.Perez |
      | firstName | Juan       |
      | lastName  | Perez      |
      | isActive  | true       |
    And the response should contain 1 year
    And the response should contain year 2026 with 1 month
    And the response should show 100 hours in February 2026

  Scenario: Retrieve workload for non-existent trainer
    Given no trainer with username "Unknown.Trainer" exists
    When I attempt to retrieve the workload for "Unknown.Trainer"
    Then the operation should fail with "Trainer with username Unknown.Trainer not found"
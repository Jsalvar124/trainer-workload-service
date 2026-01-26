Feature: Trainer workload Management
  Scenario: Add workload to existing trainer in existing month
    Given a trainer "Juan.Perez" exists with 100 hours in February 2026
    When I add 120 hours of workload for "Juan.Perez" on "2026-02-15"
    Then the trainer should have 220 total hours in February 2026
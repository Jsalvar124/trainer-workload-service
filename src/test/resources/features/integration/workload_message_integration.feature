Feature: Trainer Workload Messaging Integration
  As a system I want to process workload messages from the queue So that trainer workload is automatically tracked

  Scenario: Process message to create new trainer workload
    When a workload message is sent to the queue with the following details:
      | username   | Juan.Perez   |
      | firstName  | Juan       |
      | lastName   | Perez        |
      | isActive   | true       |
      | date       | 2024-12-15 |
      | duration   | 120         |
      | actionType | ADD        |
    Then a trainer "Juan.Perez" should exist in the database
    And trainer "Juan.Perez" should have 120 hours in December 2024

  Scenario: Process message to add workload to an existing trainer workload
    Given a trainer "Juan.Perez" exists with 100 hours in December 2024
    When a workload message is sent to the queue with the following details:
      | username   | Juan.Perez   |
      | firstName  | Juan       |
      | lastName   | Perez        |
      | isActive   | true       |
      | date       | 2024-12-15 |
      | duration   | 120         |
      | actionType | ADD        |
    Then a trainer "Juan.Perez" should exist in the database
    And trainer "Juan.Perez" should have 220 hours in December 2024
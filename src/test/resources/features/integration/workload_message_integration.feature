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
      | actionType | ADD         |
    Then a trainer "Juan.Perez" should exist in the database
    And trainer "Juan.Perez" should have 120 hours in December 2024

  Scenario Outline: Process message to update workload to an existing trainer workload
    Given a trainer "<username>" exists with <initialHours> hours in December <year>
    When a workload message is sent to the queue with the following details:
      | username   | <username>   |
      | firstName  | Juan         |
      | lastName   | Perez        |
      | isActive   | true         |
      | date       | <date>   |
      | duration   | <duration>   |
      | actionType | <action> |
    Then a trainer "<username>" should exist in the database
    And trainer "<username>" should have <expectedHours> hours in December <year>

    Examples:
      | username    | initialHours | year | date       | duration | action | expectedHours |
      | Juan.Perez  | 100          | 2024 | 2024-12-15 | 120   | ADD    | 220           |
      | Juan.Perez  | 220          | 2024 | 2024-12-15 | 120   | DELETE | 100           |


  Scenario: Invalid message is routed to Dead Letter Queue
    When an invalid workload message is sent for "Error.Test"
    Then the trainer "Error.Test" should not be created
    And the message should be in the Dead Letter Queue


  Scenario: Message with missing required fields is rejected
    When a message with missing username is sent to the queue
    Then no trainers should be created in the database
    And the message with missing fields should be in the Dead Letter Queue
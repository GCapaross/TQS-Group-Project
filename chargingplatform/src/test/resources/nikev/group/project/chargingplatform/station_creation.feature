Feature: Station Creation
  As an API client
  I want to create new charging stations via the API
  So that they are persisted in the system

  Background:
    Given the application is running

  Scenario: Successfully creating a new station
    Given a company named "ChargeCorp" exists
    When I send a POST to "/api/charging-stations" to create a station with body
      | name              | Test Station        |
      | location          | Lisbon              |
      | latitude          | 38.7223             |
      | longitude         | -9.1393             |
      | pricePerKwh       | 0.20                |
      | supportedConnectors | ["Type2"]          |
      | companyName       | ChargeCorp          |
    Then the station creation response status should be 200
    And the station creation response should contain:
      | name        | Test Station |
      | location    | Lisbon       |
      | latitude    | 38.7223      |
      | longitude   | -9.1393      |
      | pricePerKwh | 0.20         |
      | companyName | ChargeCorp   |

  Scenario: Failing to create a station with unknown company
    When I send a POST to "/api/charging-stations" to create a station with body
      | name        | Unknown Station  |
      | location    | Porto            |
      | latitude    | 41.1579          |
      | longitude   | -8.6291          |
      | pricePerKwh | 0.15             |
      | companyName | NonExistentCo    |
    Then the station creation response status should be 400
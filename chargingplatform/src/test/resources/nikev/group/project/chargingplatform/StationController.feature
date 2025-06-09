Feature: Station Creation
  As an API client
  I want to create new charging stations via the API
  So that they are persisted in the system

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
  
  @retrieveAll
  Scenario: Retrieve all stations
    When I send a GET to "/api/charging-stations"
    Then the response status should be 200
    And the response should contain an array with length 5

  @retrieveById
  Scenario Outline: Retrieving a specific station by ID
    When I send a GET to "/api/charging-stations/<id>"
    Then the response status should be <status>
    And the response should contain:
      | name         | location | latitude | longitude | pricePerKwh | companyName |
      | <name>       | <location> | <latitude> | <longitude> | <pricePerKwh> | <companyName> |

  Examples:
    | id  | status | name         | location | latitude | longitude | pricePerKwh | companyName |
    | 5   | 200    | Test Station | Lisbon   | 38.7223  | -9.1393   | 0.2        | ChargeCorp  |

  @retrieveByIdErrors
  Scenario Outline: Failing to retrieve a station with invalid ID
    When I send a GET to "/api/charging-stations/<id>"
    Then the response status should be <status>

  Examples:
    | id    | status |
    | 999   | 404    |
    | -1    | 400    |
    | abc   | 400    |

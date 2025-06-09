Feature: Booking slots and canceling reservations via API

  @book
  Scenario Outline: Booking a slot
    Given a user named "testuser" exists with email "testBook@example.com" and password "test123"
    And a company named "ChargeCorp" already exists
    And a station exists for "ChargeCorp"
    When I send an authenticated POST to "/api/bookings" with body
      | stationId  | startTime               | endTime                 |
      | <stationId> | <startTime>            | <endTime>               |
    Then the response status should be <status>
    Examples:
      | stationId | startTime                             | endTime                               | status |
      | 1         | 2025-06-10T10:00:00                   | 2025-06-10T11:00:00                   | 400    |  # no such station
      | 30         | 2020-01-01T00:00:00                   | 2020-01-01T01:00:00                   | 400    |  # in the past
      | 31         | 2025-06-10T14:30:00                   | 2025-06-10T15:30:00                   | 200    | 
      | 32         | ${now.plusMinutes(15)}               | ${now.plusMinutes(45)}               | 200    |  

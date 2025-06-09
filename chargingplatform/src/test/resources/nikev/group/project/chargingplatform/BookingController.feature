Feature: Booking slots and canceling reservations via API

  @book
  Scenario Outline: Booking a slot
    When I send an authenticated POST to "/api/bookings" with body
      | stationId  | startTime               | endTime                 |
      | <stationId> | <startTime>            | <endTime>               |
    Then the response status should be <status>
    Examples:
      | stationId | startTime                             | endTime                               | status |
      | 5         | 2025-06-10T10:00:00                   | 2025-06-10T11:00:00                   | 400    |  # no such station
      | 1         | 2020-01-01T00:00:00                   | 2020-01-01T01:00:00                   | 400    |  # in the past
      | 1         | 2025-06-10T14:30:00                   | 2025-06-10T15:30:00                   | 200    | 
      | 1         | ${now.plusMinutes(15)}               | ${now.plusMinutes(45)}               | 400    |  

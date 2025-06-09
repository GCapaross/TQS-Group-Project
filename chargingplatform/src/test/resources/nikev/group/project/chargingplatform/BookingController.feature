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

#   @cancel
#   Scenario: Cancel non-existent booking
#     When I send a DELETE to "/api/bookings/999"
#     Then the response status should be 404

#   @cancel
#   Scenario: Cancel an existing booking
#     # first create one via the happy-path booking
#     When I send a POST to "/api/bookings" with body
#       | stationId  | startTime               | endTime                 |
#       | 1          | ${now.plusMinutes(15)}  | ${now.plusMinutes(45)}  |
#     Then the response status should be 200
#     And the response should contain:
#       | id |
#       | 1 |
#     When I send a DELETE to "/api/bookings/1"
#     Then the response status should be 204
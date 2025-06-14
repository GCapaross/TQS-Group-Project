Feature: User registration and login via API

  @register
  Scenario Outline: Registering users
    When I send a POST to "/api/users/register" with body
      | email            | password        | confirmPassword   | username  | accountType |
      | <email>          | <password>      | <confirmPassword> | <username>| <type>      |
    Then the response status should be <registerStatus>

    Examples:
      | email             | password  | confirmPassword | username | type   | registerStatus |
      | new1@test.com     | pwd123    | pwd123          | newuser1 | user   | 200            |
      | new2@test.com     | pwd123    | pwd123          | newuser2 | operator   | 200            |
      | new3@test.com     | pwd123    | 321wdp          | newuser3 | user   | 400            |


  @login
  Scenario Outline: Logging in
    Given a user named "new1" exists with email "new1@test.com" and password "pwd123"
    When I send a POST to "/api/users/login" with body
      | email            | password  |
      | <email>          | <password>|
    Then the response status should be <loginStatus>

    Examples:
      | email            | password  | loginStatus |
      | new1@test.com    | pwd123    | 200         |
      | new1@test.com    | wrong     | 401         |
      | noone@test.com   | pwd123    | 401         |

  @me
  Scenario: Getting current user details
    Given a user named "newuser1" exists with email "new1@test.com" and password "pwd123"
    When I send a POST to "/api/users/login" with body
      | email         | password |
      | new1@test.com | pwd123   |
    Then the response status should be 200
    And I save the JWT cookie

    When I send an authenticated GET to "/api/users/me"
    Then the response status should be 200
    And the response should contain:
      | email         | username |
      | new1@test.com | newuser1 |
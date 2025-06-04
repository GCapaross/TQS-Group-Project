@EDISON-125
@REQ_EDISON-100
Feature: User profile, register and login
	#When a user sends an api call to /api/v1/login with the body
	#{"username": "User", "password": "safepassword123"}
	#Then he receives back a 200 request with his account details
	@TEST_EDISON-124 @Tests
	Scenario Outline: Test if user can log-in
		Given an account with email "test@example.com" and password "password"
		And the user is on the login page
		When the user enters the email "test@example.com" and password "password"
		And the user clicks on the login button
		Then the user should be redirected to the homepage
		

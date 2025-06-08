@REQ_EDISON-3
Feature: Booking & Reservation Management
	#Booking interface with time slot selection, conflict detection, reservation confirmation.

	Background:
		#@PRECOND_EDISON-162
		Background: Logged In and with Stations
		  Given my credentials are "test@example.com" and "examplePassword"
		  And that the Following Stations exists in the database
		    | Coimbra | 40.20564  | -8.41955 | 30, 40, 50 | CCS, Type 1 |
		    | Porto   | 41.14961  | -8.61099 | 30, 50, 70 | CCS, Type 2 |
		    | Lisboa  | 38.71667  | -9.13333 | 30         | CCS			|

	#Given I am on Map View
	#When I click on station 1
	#Then I get redirected to book page
	@TEST_EDISON-156 @TESTSET_EDISON-161
	Scenario: Test if Users can go to reservation page through map view
		Given I am on Map View
		When I click on station 1
		Then I get redirected to book page
		
	#Tests Booking interface with time slot selection, conflict detection, reservation confirmation.
	@TEST_EDISON-159 @TESTSET_EDISON-161
	Scenario: Test if booking succeds when I add valid overlapping reservation
		Given I am on Book page
		And a reservation for "tomorrow" from 14h00 until 14h30 exists
		And a reservation for "tomorrow" from 14h30 until 15h00 exists
		And I click on book button
		And set Start Date to "tomorrow" at 14h15
		And set End Date to "tommorrow" at 14h45
		And set Estimated Energy Needed to 120
		When I click book now
		Then confirmation popup appears
		
#	@TEST_EDISON-160 @TESTSET_EDISON-161
#	Scenario: Test if booking fails when no charger available
#		Given I am on Book page
#		And a reservation for "tomorrow" from 14h00 until 14h30 exists
#		And a reservation for "tomorrow" from 14h30 until 15h00 exists
#		And I click on book button
#		And set Start Date to "tomorrow" at 14h15
#		And set End Date to "tommorrow" at 14h45
#		And set Estimated Energy Needed to 120
#		When I click book now
#		Then confirmation popup appears
#		
#	#Given I am on Book page
#	#And I click on book button
#	#And set Start Date to "tomorrow" at 14h00
#	#And set End Date to "tommorrow" at 14h30
#	#And set Estimated Energy Needed to 120
#	#When I click book now
#	#Then confirmation popup appears
#	@TEST_EDISON-157 @TESTSET_EDISON-161
#	Scenario: Test if users set valid booking details, then booking is made
#		Given I am on Book page
#		And I click on book button
#		And set Start Date to "tomorrow" at 14h00
#		And set End Date to "tommorrow" at 14h30
#		And set Estimated Energy Needed to 120
#		When I click book now
#		Then confirmation popup appears
#		
#	#Given I am on Book page
#	#And I click on book button
#	#And set Start Date to "tomorrow" at 14h00
#	#And set End Date to "tommorrow" at 19h00
#	#And set Estimated Energy Needed to 120
#	#When I click book now
#	#Then booking fails
#	@TEST_EDISON-158 @TESTSET_EDISON-161
#	Scenario: Test if users create reservations with more than 4 hours, then booking fails
#		Given I am on Book page
#		And I click on book button
#		And set Start Date to "tomorrow" at 14h00
#		And set End Date to "tommorrow" at 19h00
#		And set Estimated Energy Needed to 120
#		When I click book now
#		Then booking fails
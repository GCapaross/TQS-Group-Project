Feature: Test Station Discovery & Filtering

	Background:
		#@PRECOND_EDISON-162
		Background: Logged In and with Stations
		  Given my credentials are "test@example.com" and "examplePassword"
		  And that the Following Stations exists in the database
		    | Coimbra | 40.20564  | -8.41955 | 30, 40, 50 | CCS, Type 1 |
		    | Porto   | 41.14961  | -8.61099 | 30, 50, 70 | CCS, Type 2 |
		    | Lisboa  | 38.71667  | -9.13333 | 30         | CCS         |

	@TEST_EDISON-153 @TESTSET_EDISON-152
	Scenario: Test if user can see all stations on map
		Given I am on home page
		When I click on "Map View"
		Then 3 Stations appear
		
	@TEST_EDISON-154 @TESTSET_EDISON-152
	Scenario: Test if charging filter works on Map
		Given I am on Map View
		When I set minimum charge speed to 40
		Then 2 Stations appear
		
	@TEST_EDISON-155 @TESTSET_EDISON-152
	Scenario: Test if Connector filter works on map
		Given I am on Map View
		When I set minimun charge speed to 40
		And I set supported connectors to "Type 1"
		Then 1 Station appear
		

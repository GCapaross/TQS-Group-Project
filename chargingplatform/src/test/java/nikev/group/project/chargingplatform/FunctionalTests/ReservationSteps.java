package nikev.group.project.chargingplatform.FunctionalTests;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.DTOs.StationCreateDTO;
import nikev.group.project.chargingplatform.DTOs.StationDTO;
import nikev.group.project.chargingplatform.DTOs.StationResponseDTO;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.service.StationService;
import nikev.group.project.chargingplatform.service.UserService;

import org.checkerframework.checker.units.qual.t;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public class ReservationSteps {
    private WebDriver driver;
    private String frontendHost = "localhost";
    private String frontendPort = "5173";
    private static String BASE_URL;
    
    // Services
    @Autowired
    private UserService userService;
    @Autowired
    private StationService stationService;
    @Autowired
    private CompanyRepository companies;

    // Repositories
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private ChargerRepository chargerRepository;
    @Autowired
    private UserRepository UserRepository;

    public ReservationSteps() {
        driver = Hooks.driver;
        BASE_URL = "http://" + frontendHost + ":" + frontendPort;
    }

    @After
    public void closeUp() {
        driver.quit();
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
        companies.deleteAll();
        UserRepository.deleteAll();
    }

    @Given("my credentials are {string} and {string}")
    public void my_credentials_are_and(String email, String password) {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "John Doe",
            password,
            password,
            email,
            "user"
        );
        userService.registerUser(request);

        // Login to the application
        driver.get(BASE_URL + "/login");
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys(email);
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys(password);
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();
    }

    @Given("that the Following Stations exists in the database")    
    public void that_the_following_stations_exists_in_the_database(io.cucumber.datatable.DataTable dataTable) {
        Company company = new Company();
        company.setName("TestCompany");
        company = companies.save(company);

        List<List<String>> rawRows = dataTable.asLists(String.class);
        for (List<String> row : rawRows) {
            String name = row.get(0);
            String location = row.get(0);
            double latitude = Double.parseDouble(row.get(1));
            double longitude = Double.parseDouble(row.get(2));
            double pricePerKwh = 0.13d;
            List<String> supportedConnectors = List.of(row.get(3).split(","));
            List<String> chargersSpeed = List.of(row.get(4).split(","));


            StationCreateDTO station = new StationCreateDTO(
                name,
                location,
                latitude,
                longitude,
                pricePerKwh,
                supportedConnectors,
                "timetable",
                "TestCompany",
                List.of(), // List of worker IDs
                List.of() // Assuming no chargers for now
            );
            StationResponseDTO createdStationDTO = stationService.createStation( 
                station
            );

            Station createdStation = stationRepository.findById(createdStationDTO.getId())
                .orElseThrow(() -> new RuntimeException("Station not found after creation"));
            
            chargersSpeed.stream()
                .map(speed -> {
                    Charger charger = new Charger();
                    charger.setStatus(Charger.ChargerStatus.AVAILABLE);
                    charger.setChargingSpeedKw(Double.parseDouble(speed));
                    charger.setStation(createdStation);
                    charger = chargerRepository.save(charger);
                    return charger;
                });
        }
    }

    @Given("I am on Book page")
    public void i_am_on_book_page() {
        StationDTO station = stationService.getAllStations().get(0);
        driver.get(BASE_URL + "/stations/" + station.getId() + "/book");
        try {
            Thread.sleep(10_000L); // Wait for the page to load
        } catch (Exception e) {}
    }

    @Given("I am on Map View") 
    public void i_am_on_map_view() {
        driver.get(BASE_URL + "/map");
        try {
            Thread.sleep(50_000L); // Wait for the page to load
        } catch (Exception e) {}
    }

    @Given("I click on book button")
    public void i_click_on_book_button() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("set Start Date to {string} at {int}h{int}")
    public void set_start_date() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("set End Date to {string} at {int}h{int}")
    public void set_end_date(String date, Integer hours, Integer minutes) {
        LocalDate reservationDate = string_to_date(date);
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("set Estimated Energy Needed to {int}")
    public void set_estimated_energy_needed_to(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("I click book now")
    public void i_click_book_now() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


    @Then("confirmation popup appears")
    public void confirmation_popup_appears() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
    @Then("booking fails")
    public void booking_fails() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


    private LocalDate string_to_date(String date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'string_to_date'");
    }
}

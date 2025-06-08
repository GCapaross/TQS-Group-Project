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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.DTOs.StationCreateDTO;
import nikev.group.project.chargingplatform.DTOs.StationDTO;
import nikev.group.project.chargingplatform.DTOs.StationResponseDTO;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.service.StationService;
import nikev.group.project.chargingplatform.service.UserService;

import org.checkerframework.checker.units.qual.t;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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


import org.openqa.selenium.interactions.Actions;


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
    @Autowired
    private ReservationRepository reservationRepository;

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
// @Given("a reservation for tomorrow from 14h00 until 14h30 exists")
    @Given("a reservation for {string} from {int}h{int} until {int}h{int} exists")
    public void a_reservation_exists(
        String date,
        Integer startHours, Integer startMinutes,
        Integer endHours, Integer endMinutes
    ) {
        // Create a reservation for tomorrow from 14:00 to 14:30
        LocalDate reservationDate = string_to_date(date);
        LocalDateTime startDate = reservationDate.atTime(startHours, startMinutes);
        LocalDateTime endDate = reservationDate.atTime(endHours, endMinutes);

        StationDTO station = stationService.getAllStations().get(0);
        Station stationEntity = stationRepository.findById(station.getId())
            .orElseThrow(() -> new RuntimeException("Station not found"));
        
        Reservation reservation = new Reservation();
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStation(stationEntity);
        reservation = reservationRepository.save(reservation);
    }


/*
Given I am on Book page                                                  # nikev.group.project.chargingplatform.FunctionalTests.ReservationSteps.i_am_on_book_page()
org.hibernate.LazyInitializationException: Could not initialize proxy [nikev.group.project.chargingplatform.model.Company#3] - no session
at org.hibernate.proxy.AbstractLazyInitializer.initialize(AbstractLazyInitializer.java:174)
at org.hibernate.proxy.AbstractLazyInitializer.getImplementation(AbstractLazyInitializer.java:328)
at org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor.intercept(ByteBuddyInterceptor.java:44)
at org.hibernate.proxy.ProxyConfiguration$InterceptorDispatcher.intercept(ProxyConfiguration.java:102)
at nikev.group.project.chargingplatform.model.Company$HibernateProxy$Mt9qqYWP.getName(Unknown Source)
at nikev.group.project.chargingplatform.service.StationService.convertToStationDTO(StationService.java:285)
at nikev.group.project.chargingplatform.service.StationService.getAllStations(StationService.java:55)
at nikev.group.project.chargingplatform.FunctionalTests.ReservationSteps.i_am_on_book_page(ReservationSteps.java:187)
at ✽.I am on Book page(classpath:nikev/group/project/chargingplatform/1_EDISON-3_EDISON-162.feature:26)
*/
    @Given("I am on Book page")
    public void i_am_on_book_page() {
        Station station = stationRepository.findAll().get(0);
        driver.get(BASE_URL + "/stations/" + station.getId() + "/book");
    }

    @Given("I am on Map View") 
    public void i_am_on_map_view() {
        driver.get(BASE_URL + "/map");
        // Wait for the map to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("map"))); // adjust to your map's ID or class
        WebElement mapElement = driver.findElement(By.id("map")); // adjust to your map's ID or class

        WebElement zoomOutButton = driver.findElement(By.className("leaflet-control-zoom-out"));
        for (int i = 0; i < 6; i++) {
            zoomOutButton.click();
            try {
                Thread.sleep(500); // wait for the map to update
            } catch (InterruptedException e) {}
        }
    }
    
    @Given("I click on book button")
    public void i_click_on_book_button() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement bookButton = driver.findElement(By.id("book-button"));
        bookButton.click();
    }
    
    @Given("I click on station {int}") 
    public void i_click_on_station(Integer stationIdx) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        List<WebElement> marker = driver.findElements(By.className("leaflet-marker-icon"));

        marker.get(stationIdx).click();
        driver.findElement(By.id("book-button")).click();
    }

    @Given("set Start Date to {string} at {int}h{int}")
    public void set_start_date(String startDate, Integer hours, Integer minutes) {
        // Write code here that turns the phrase above into concrete actions
        LocalDate reservationDate = string_to_date(startDate);
        // start-date and end-date
        WebElement startDateInput = driver.findElement(By.id("start-date"));
        startDateInput.clear();

        LocalDateTime startDateTime = reservationDate.atTime(hours, minutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String startDateTimeFormatted = startDateTime.format(formatter);
        startDateInput.sendKeys(startDateTimeFormatted);
    }

    @Given("set End Date to {string} at {int}h{int}")
    public void set_end_date(String endDate, Integer hours, Integer minutes) {
        LocalDate reservationDate = string_to_date(endDate);
        // Write code here that turns the phrase above into concrete actions
        WebElement endDateInput = driver.findElement(By.id("end-date"));
        // endDateInput.clear();
        // endDateInput.sendKeys(reservationDate.toString() + "T" + String.format("%02d:%02d", hours, minutes));
        LocalDateTime endDateTime = reservationDate.atTime(hours, minutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String endDateTimeFormatted = endDateTime.format(formatter);
        endDateInput.clear();
        endDateInput.sendKeys(endDateTimeFormatted);
    }

    @Given("set Estimated Energy Needed to {string}")
    public void set_estimated_energy_needed_to(String energy) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement energyInput = driver.findElement(By.id("estimated-energy"));
        energyInput.clear();
        energyInput.sendKeys(energy);
    }

    @When("I click book now")
    public void i_click_book_now() {
        // Write code here that turns the phrase above into concrete actions
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement bookNowButton = driver.findElement(By.id("book-button"));
        bookNowButton.click();
        // Wait for the confirmation popup to appear
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirmation-id")));
    }
    
    @Then("I get redirected to book page")
    public void then_i_get_redirected_to_book_page() {
        String currentUrl = driver.getCurrentUrl();
        assertThat(currentUrl).contains("/book");
    }
    
    @Then("confirmation popup appears")
    public void confirmation_popup_appears() {
        // Write code here that turns the phrase above into concrete actions
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement confirmationPopup = driver.findElement(By.id("confirmation-button"));
        assertThat(confirmationPopup.isDisplayed()).isTrue();
    }

    @Then("booking fails")
    public void booking_fails() {
        // Write code here that turns the phrase above into concrete actions
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));	
        WebElement errorMessage = driver.findElement(By.id("error-message"));
        assertThat(errorMessage.isDisplayed()).isTrue();
    }



    private LocalDate string_to_date(String date) {
        switch (date) {
            case "yesterday":
                return LocalDate.now().minusDays(1);
            case "today":
                return LocalDate.now();
            case "tomorrow":
                return LocalDate.now().plusDays(1);
            default:
                break;
        }
        // If the date is not a special case, parse it as a LocalDate
        return null;
    }
}

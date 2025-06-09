package nikev.group.project.chargingplatform.FunctionalTests;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.DTOs.StationCreateDTO;
import nikev.group.project.chargingplatform.DTOs.StationDTO;
import nikev.group.project.chargingplatform.DTOs.StationResponseDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.service.StationService;
import nikev.group.project.chargingplatform.service.UserService;


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
    private ReservationRepository reservationRepository;

    public ReservationSteps() {
        driver = Hooks.driver;
        BASE_URL = "http://" + frontendHost + ":" + frontendPort;
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
        companies.save(company);

        List<List<String>> rawRows = dataTable.asLists(String.class);
        for (List<String> row : rawRows) {
            String name = row.get(0);
            String location = row.get(0);
            double latitude = Double.parseDouble(row.get(1));
            double longitude = Double.parseDouble(row.get(2));
            double pricePerKwh = 0.13d;
            List<String> chargersSpeed = List.of(row.get(3).split(","));
            List<String> supportedConnectors = List.of(row.get(4).split(","));

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
                .forEach(speed -> {
                    Charger charger = new Charger();
                    charger.setStatus(Charger.ChargerStatus.AVAILABLE);
                    charger.setChargingSpeedKw(Double.parseDouble(speed));
                    charger.setStation(createdStation);
                    charger = chargerRepository.save(charger);
                });
            chargerRepository.flush();

            List<Charger> chargers = chargerRepository.findByStation_Id(createdStation.getId());
            chargers.forEach(charger -> {
                System.out.println("Charger ID: " + charger.getId() + ", Speed: " + charger.getChargingSpeedKw() + 
                    ", Status: " + charger.getStatus());
            });
        }
    }

    @Transactional  
    @Given("a reservation for {string} from {int}h{int} until {int}h{int} exists")
    public void a_reservation_exists(
        String date,
        Integer startHours, Integer startMinutes,
        Integer endHours, Integer endMinutes
    ) {
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
        reservationRepository.save(reservation);
    }

    @Given("I am on Book page")
    public void i_am_on_book_page() {
        Station station = stationRepository.findAll().get(0);
        driver.get(BASE_URL + "/stations/" + station.getId() + "/book");
    }

    @Given("I am on Map View") 
    public void i_am_on_map_view() {
        driver.get(BASE_URL + "/map");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("map")));

        WebElement zoomOutButton = driver.findElement(By.className("leaflet-control-zoom-out"));
        for (int i = 0; i < 6; i++) {
            zoomOutButton.click();
            try {
                Thread.sleep(500); // wait for the map to update
            } catch (InterruptedException e) {
                // No need to handle exception
            }
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
        LocalDate reservationDate = string_to_date(startDate);
        // start-date and end-date
        WebElement startDateInput = driver.findElement(By.id("start-date"));
        startDateInput.clear();

        LocalDateTime startDateTime = reservationDate.atTime(hours, minutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyyhhmma");
        String startDateTimeFormatted = startDateTime.format(formatter);
        startDateInput.sendKeys(startDateTimeFormatted);
    }

    @Given("set End Date to {string} at {int}h{int}")
    public void set_end_date(String date, Integer hours, Integer minutes) {
        LocalDate reservationDate = string_to_date(date);

        WebElement endDateInput = driver.findElement(By.id("end-date"));
        LocalDateTime endDateTime = reservationDate.atTime(hours, minutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyyhhmma");
        String endDateTimeFormatted = endDateTime.format(formatter);
        endDateInput.clear();
        endDateInput.sendKeys(endDateTimeFormatted);
    }

    @Given("set Estimated Energy Needed to {int}")
    public void set_estimated_energy_needed_to(Integer energy) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement energyInput = driver.findElement(By.id("estimated-energy"));
        energyInput.clear();
        energyInput.sendKeys(energy.toString());
    }

    @When("I click book now")
    public void i_click_book_now() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement bookNowButton = driver.findElement(By.id("confirmation-button"));
        bookNowButton.click();
    }
    
    @Then("I get redirected to book page")
    public void then_i_get_redirected_to_book_page() {
        String currentUrl = driver.getCurrentUrl();
        assertThat(currentUrl).contains("/book");
    }
    
    @Then("confirmation popup appears")
    public void confirmation_popup_appears() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement confirmationPopup = driver.findElement(By.id("booking-id"));
        assertThat(confirmationPopup.isDisplayed()).isTrue();
    }

    @Then("booking fails")
    public void booking_fails() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));	
        WebElement errorMessage = driver.findElement(By.id("error-message"));
        assertThat(errorMessage.isDisplayed()).isTrue();
    }



    private LocalDate string_to_date(String date) {
        LocalDate parsedDate = null;
        switch (date) {
            case "yesterday":
                parsedDate = LocalDate.now().minusDays(1);
                break;
            case "today":
                parsedDate = LocalDate.now();
                break;
            case "tomorrow":
                parsedDate = LocalDate.now().plusDays(1);
                break;
            default:
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                parsedDate = LocalDate.parse(date, formatter);
        }
        return parsedDate;
    }
}

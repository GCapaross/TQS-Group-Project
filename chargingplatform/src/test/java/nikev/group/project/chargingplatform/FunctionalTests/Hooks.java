package nikev.group.project.chargingplatform.FunctionalTests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;

public class Hooks {
    public static WebDriver driver;

    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private ChargerRepository chargerRepository;
    @Autowired
    private UserRepository UserRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private CompanyRepository companies;
    
    @Before
    public void setUp() {
        clearDb();
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
    }

    @After
    public void cleanUp() {
        if (null != driver) {
            driver.quit();
        }
    }

    private void clearDb() {
        reservationRepository.deleteAll();
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
        companies.deleteAll();
        UserRepository.deleteAll();
        WebDriverManager.chromedriver().setup();
    }
}
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

import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.UserService;

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


public class LoginSteps {

    private WebDriver driver;
    private static final String BASE_URL = "http://localhost:5173";

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
    }

    @After
    public void closeUp() {
        driver.close();
    }

    @Autowired
    private UserService userService;

    @Given("an account with email {string} and password {string}")
    public void givenTheUserIsOnTheHomepage(String email, String password) {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "John Doe",
            password,
            password,
            email,
            "user"
        );
        userService.registerUser(request);
    }

    @And("the user is on the login page")
    public void andTheUserIsOnTheLoginPage() {
        driver.get(BASE_URL + "/login");
    }

    @When("the user enters the email {string} and password {string}")
    public void whenTheUserEntersTheEmailAndPassword(
        String email,
        String password
    ) {
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));

        emailField.sendKeys(email);
        passwordField.sendKeys(password);
    }

    @And("the user clicks on the login button")
    public void andTheUserClicksTheLoginButton() {
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();
    }

    @Then("the user should be redirected to the homepage")
    public void thenTheUserShouldBeRedirectedToTheHomepage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(
            ExpectedConditions.not(ExpectedConditions.urlContains("/login"))
        );
        assertThat(driver.getCurrentUrl()).isEqualTo(BASE_URL + "/");
    }
}

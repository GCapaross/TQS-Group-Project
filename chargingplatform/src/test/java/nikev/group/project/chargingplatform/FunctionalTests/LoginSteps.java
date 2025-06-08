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
import org.springframework.beans.factory.annotation.Value;


public class LoginSteps {
    private WebDriver driver;
    private String frontendHost = "localhost";
    private String frontendPort = "5173";

    private static String BASE_URL;

    public LoginSteps() {
        driver = Hooks.driver;
        BASE_URL = "http://" + frontendHost + ":" + frontendPort;
    }

    @Autowired
    private UserService userService;

    @Given("an account with email {string} and password {string}")
    public void givenARegisteredUser(String email, String password) {
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
        System.out.println("\n\nNavigating to login page: " + BASE_URL + "/login");
        System.out.println("Driver is initialized: " + (driver != null) + "\n\n");
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

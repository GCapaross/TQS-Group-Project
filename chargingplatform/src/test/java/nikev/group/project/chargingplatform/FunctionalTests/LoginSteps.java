package nikev.group.project.chargingplatform.FunctionalTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.sql.DriverManager;
import java.time.Duration;
import java.util.List;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.ProfilesIni;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class LoginSteps {

  private WebDriver driver;
  private static final String BASE_URL = "http://localhost:5173";

  @Before
  public void setUp() {
    driver = WebDriverManager.chromedriver().create();
  }

  @After
  public void closeUp() {
    driver.close();
  }

  @Autowired
  private UserService userService;

  @Given("an account with email {string} and password {string}")
  public void givenTheUserIsOnTheHomepage(String email, String password) {
    User user = new User();
    user.setEmail(email);
    user.setPassword(password);
    user.setName("Test User");
    userService.registerUser(user);
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
    driver.findElement(By.id("login-button")).click();
    assertThat(driver.getCurrentUrl()).isEqualTo(BASE_URL);
  }
}

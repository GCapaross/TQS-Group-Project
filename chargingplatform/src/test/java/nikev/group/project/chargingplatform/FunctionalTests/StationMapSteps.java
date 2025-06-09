package nikev.group.project.chargingplatform.FunctionalTests;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.bidi.log.JavascriptLogEntry;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web;

import ch.qos.logback.core.joran.action.Action;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import nikev.group.project.chargingplatform.service.StationService;
import nikev.group.project.chargingplatform.service.UserService;

public class StationMapSteps {
    private WebDriver driver;
    private String frontendHost = "localhost";
    private String frontendPort = "5173";
    private static String BASE_URL;

    public StationMapSteps() {
        driver = Hooks.driver;
        BASE_URL = "http://" + frontendHost + ":" + frontendPort;
    }

    @When("I set supported connectors to {string}")
    public void i_set_supported_connectors_to(String type) {
        WebElement selectorElement = driver.findElement(By.id("filter-connectors"));
        selectorElement.click();

        List<WebElement> option = driver.findElements(By.className("MuiMenuItem-gutters"));
        for (WebElement optionElement : option) {
            if (optionElement.getText().equals(type)) {
                optionElement.click();
                break;
            }
        }
    }

    @When("I set minimum charge speed to {int}")
    public void i_set_minimum_charge_speed_to(Integer chargeSpeed) {
        WebElement chargeSpeedElement = driver.findElement(By.id("min-charge-slider"));
        WebElement inputElement = chargeSpeedElement.findElement(By.tagName("input"));

        // Use JavaScript to set the value of the input element
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = 
            "const slider = arguments[0];" +
            "const targetValue = arguments[1];" +
            
            // Find the slider input element (hidden or visible)
            "const input = slider.querySelector('input') || slider;" +
            
            // Create a React synthetic event that updates useState
            "const nativeInputValueSetter = Object.getOwnPropertyDescriptor(" +
            "  window.HTMLInputElement.prototype, 'value').set;" +
            "nativeInputValueSetter.call(input, targetValue);" +
            
            // Dispatch input event (React listens to this)
            "const inputEvent = new Event('input', { bubbles: true });" +
            "Object.defineProperty(inputEvent, 'target', {writable: false, value: input});" +
            "Object.defineProperty(inputEvent, 'currentTarget', {writable: false, value: input});" +
            "input.dispatchEvent(inputEvent);" +
            
            // Also dispatch change event for good measure
            "const changeEvent = new Event('change', { bubbles: true });" +
            "Object.defineProperty(changeEvent, 'target', {writable: false, value: input});" +
            "Object.defineProperty(changeEvent, 'currentTarget', {writable: false, value: input});" +
            "input.dispatchEvent(changeEvent);";
        js.executeScript(script, chargeSpeedElement, chargeSpeed);
    }

    @Then("{int} Stations appear")
    public void stations_appear(Integer numStations) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        List<WebElement> marker = driver.findElements(By.className("leaflet-marker-icon"));
        assertThat(marker, hasSize(numStations));
    }
}

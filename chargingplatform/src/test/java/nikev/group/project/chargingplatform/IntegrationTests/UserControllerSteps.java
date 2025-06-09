package nikev.group.project.chargingplatform.IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.*;

public class UserControllerSteps {

  @Autowired
  private TestRestTemplate rest;

  private ResponseEntity<String> response;
  public static String jwtToken;       // will hold the raw cookie value
  private final ObjectMapper mapper = new ObjectMapper();

  @When("I send a POST to {string} with body")
  public void i_send_post_with_body(String path, DataTable table) {
    var map = table.asMaps().get(0);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // build JSON body from the table columns
    StringBuilder body = new StringBuilder("{");
    for (var entry : map.entrySet()) {
      if (entry.getValue() != null) {
        body.append("\"")
            .append(entry.getKey())
            .append("\":\"")
            .append(entry.getValue())
            .append("\",");
      }
    }
    // remove trailing comma, close object
    if (body.charAt(body.length()-1)==',') {
      body.setLength(body.length()-1);
    }
    body.append("}");

    response = rest.exchange(path, HttpMethod.POST,
      new HttpEntity<>(body.toString(), headers),
      String.class);
  }

  @When("I send an authenticated POST to {string} with body")
  public void i_send_auth_post_with_body(String path, DataTable table) {
    var map = table.asMaps().get(0);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    // add JWT cookie if present
    if (jwtToken != null) {
      headers.add(HttpHeaders.COOKIE, "JWT_TOKEN=" + jwtToken);
    }
    // build JSON body from the table columns
    StringBuilder body = new StringBuilder("{");
    for (var entry : map.entrySet()) {
      if (entry.getValue() != null) {
        body.append("\"")
            .append(entry.getKey())
            .append("\":\"")
            .append(entry.getValue())
            .append("\",");
      }
    }
    // remove trailing comma, close object
    if (body.charAt(body.length() - 1) == ',') {
      body.setLength(body.length() - 1);
    }
    body.append("}");
    response = rest.exchange(
      path,
      HttpMethod.POST,
      new HttpEntity<>(body.toString(), headers),
      String.class
    );
  }

  @Then("I save the JWT cookie")
  public void i_save_the_jwt_cookie() {
    // look for Set-Cookie header
    HttpHeaders respHeaders = response.getHeaders();
    String setCookie = respHeaders.getFirst(HttpHeaders.SET_COOKIE);
    if (setCookie == null) {
      throw new IllegalStateException("JWT_TOKEN cookie must be present");
    }
    jwtToken = setCookie.split(";")[0].split("=")[1];
  }

  @When("I send a GET to {string}")
  public void i_send_get_to(String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (jwtToken != null) {
      headers.add(HttpHeaders.COOKIE, "JWT_TOKEN=" + jwtToken);
    }
    response = rest.exchange(path, HttpMethod.GET,
      new HttpEntity<>(headers),
      String.class);
  }

  @Then("the response should contain:")
  public void the_response_should_contain(DataTable table) throws Exception {
    var map = table.asMaps().get(0);
    String responseBody = response.getBody();
    JsonNode jsonResponse = mapper.readTree(responseBody);
    for (var entry : map.entrySet()) {
      String key = entry.getKey();
      String expectedValue = entry.getValue();
      JsonNode actualValueNode = jsonResponse.path(key);
      if (actualValueNode.isMissingNode()) {
        throw new IllegalStateException("Key '" + key + "' not found in response");
      }
      String actualValue = actualValueNode.asText();
      assertThat(actualValue).isEqualTo(expectedValue);
    }
  }

  @Then("the response status should be {int}")
  public void the_response_status_should_be(Integer expected) {
    assertThat(response.getStatusCode().value()).isEqualTo(expected);
  }
}
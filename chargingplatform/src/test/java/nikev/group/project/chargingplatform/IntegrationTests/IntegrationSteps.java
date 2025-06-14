package nikev.group.project.chargingplatform.IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.repository.StationRepository;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegrationSteps {

  @Autowired
  private TestRestTemplate rest;

  private ResponseEntity<String> response;
  public static String jwtToken;       // will hold the raw cookie value
  private final ObjectMapper mapper = new ObjectMapper();
  private static final Pattern NOW_PLUS_MINUTES = Pattern.compile("\\$\\{now\\.plusMinutes\\((\\d+)\\)\\}");

  @Given("a user named {string} exists with email {string} and password {string}")
  public void a_user_named_exists_with_email_and_password(
    String username, String email, String password) {
    var register = Map.of(
      "username", username,
      "email", email,
      "password", password,
      "confirmPassword", password,
      "accountType", "user"
    );
    rest.postForEntity("/api/users/register", register, String.class);

    // login and capture JWT cookie
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var loginBody = Map.of("email", email, "password", password);
    ResponseEntity<String> resp = rest.exchange(
      "/api/users/login",
      HttpMethod.POST,
      new HttpEntity<>(loginBody, headers),
      String.class
    );
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    if (setCookie == null) {
      throw new IllegalStateException("JWT_TOKEN cookie must be present");
    }
    jwtToken = setCookie.split(";")[0].split("=")[1];
  }

  @When("I send a POST to {string} with body")
  public void i_send_post_with_body(String path, DataTable table) {
    var map = table.asMaps().get(0);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    StringBuilder body = new StringBuilder("{");
    for (var entry : map.entrySet()) {
      String raw = entry.getValue();
      if (raw == null) continue;
      // replace ${now.plusMinutes(N)} placeholders
      Matcher m = NOW_PLUS_MINUTES.matcher(raw);
      if (m.matches()) {
        long mins = Long.parseLong(m.group(1));
        raw = LocalDateTime.now()
                .plusMinutes(mins)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      }
      body.append("\"")
          .append(entry.getKey())
          .append("\":\"")
          .append(raw)
          .append("\",");
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
    StringBuilder body = new StringBuilder("{");
    for (var entry : map.entrySet()) {
      String raw = entry.getValue();
      if (raw == null) continue;
      Matcher m = NOW_PLUS_MINUTES.matcher(raw);
      if (m.matches()) {
        long mins = Long.parseLong(m.group(1));
        raw = LocalDateTime.now()
                .plusMinutes(mins)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      }
      body.append("\"")
          .append(entry.getKey())
          .append("\":\"")
          .append(raw)
          .append("\",");
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

  @When("I send an authenticated GET to {string}")
  public void i_send_auth_get_to(String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (jwtToken != null) {
      headers.add(HttpHeaders.COOKIE, "JWT_TOKEN=" + jwtToken);
    }
    response = rest.exchange(path, HttpMethod.GET,
      new HttpEntity<>(headers),
      String.class);
  }

  @When("I send a GET to {string}")
  public void i_send_get_to(String path) {
    response = rest.exchange(path, HttpMethod.GET, HttpEntity.EMPTY, String.class);
  }

  @Then("the response should contain:")
  public void the_response_should_contain(DataTable table) throws Exception {
    System.out.println("Response: " + response.getBody());
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

  @Then("the response should contain an array with length {int}")
  public void the_response_should_contain_array_with_length(Integer expectedLength) throws Exception {
    String responseBody = response.getBody();
    JsonNode jsonResponse = mapper.readTree(responseBody);
    assertThat(jsonResponse.isArray()).isTrue();
    assertThat(jsonResponse.size()).isEqualTo(expectedLength);
  }

  @Then("the response status should be {int}")
  public void the_response_status_should_be(Integer expected) {
    assertThat(response.getStatusCode().value()).isEqualTo(expected);
  }
}
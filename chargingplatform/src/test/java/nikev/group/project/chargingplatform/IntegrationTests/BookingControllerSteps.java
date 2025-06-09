package nikev.group.project.chargingplatform.IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.*;

public class BookingControllerSteps {

  @Autowired
  private TestRestTemplate rest;
  private final ObjectMapper mapper = new ObjectMapper();
  private ResponseEntity<String> response;
  private String jwtToken;

  // reuse your existing “I save the JWT cookie” from UserControllerSteps

  @When("I send a POST to {string} with body")
  public void i_send_post_with_body(String path, DataTable table) {
    var map = table.asMaps().get(0);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (jwtToken != null) {
      headers.add(HttpHeaders.COOKIE, "JWT_TOKEN=" + jwtToken);
    }

    StringBuilder body = new StringBuilder("{");
    map.forEach((k,v) -> {
      if (v!=null) body.append("\"").append(k).append("\":\"").append(v).append("\",");
    });
    if (body.charAt(body.length()-1)==',') body.setLength(body.length()-1);
    body.append("}");

    response = rest.exchange(path, HttpMethod.POST,
      new HttpEntity<>(body.toString(), headers),
      String.class);
  }

  @When("I send a DELETE to {string}")
  public void i_send_delete_to(String path) {
    HttpHeaders headers = new HttpHeaders();
    if (jwtToken != null) {
      headers.add(HttpHeaders.COOKIE, "JWT_TOKEN=" + jwtToken);
    }
    response = rest.exchange(path, HttpMethod.DELETE,
      new HttpEntity<>(headers),
      String.class);
  }

  @Then("the response status should be {int}")
  public void the_response_status_should_be(Integer expected) {
    assertThat(response.getStatusCodeValue()).isEqualTo(expected);
  }

  @Then("the response should contain:")
  public void the_response_should_contain(DataTable table) throws Exception {
    JsonNode json = mapper.readTree(response.getBody());
    var map = table.asMaps().get(0);
    map.forEach((k,v) -> {
      assertThat(json.path(k).asText()).isEqualTo(v);
    });
  }
}
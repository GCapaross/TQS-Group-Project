package nikev.group.project.chargingplatform.IntegrationTests;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.Map;

public class BookingControllerSteps {

  @Autowired
  private TestRestTemplate rest;

  // share these with UserControllerSteps via static fields
  private static boolean userSetupDone = false;

  @Before("@book")
  public void oneTimeUserSetup() {
    if (userSetupDone) return;

    // 1) register
    var register = Map.of(
      "email", "testBook@user.com",
      "password", "pwd123",
      "confirmPassword", "pwd123",
      "username", "testBook",
      "accountType", "user"
    );
    rest.postForEntity("/api/users/register", register, String.class);

    // 2) login & capture JWT cookie for reuse
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    var loginBody = Map.of("email","testBook@user.com","password","pwd123");
    ResponseEntity<String> resp = rest.exchange(
      "/api/users/login",
      HttpMethod.POST,
      new HttpEntity<>(loginBody,h),
      String.class
    );
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    if (setCookie == null) {
      throw new IllegalStateException("JWT_TOKEN cookie must be present");
    }
    UserControllerSteps.jwtToken = setCookie.split(";")[0].split("=")[1];

    userSetupDone = true;
  }

  // … now only your DELETE step or booking‐specific steps remain …
}
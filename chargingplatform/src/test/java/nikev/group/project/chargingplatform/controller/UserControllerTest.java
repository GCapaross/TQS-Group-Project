package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.UserService;
import org.flywaydb.core.internal.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
public class UserControllerTest {

  @MockitoBean
  private UserService userService;

  @Autowired
  MockMvc mockMvc;

  /* FUNCTION public ResponseEntity<User> registerUser(User user) */
  /*
   * GIven no user with email test@example.com exists
   * When user registers with email test@example.com registers
   * Then user is created with email test@example.com
   */
  @Test
  public void testRegisterUser_Success() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setName("Test User");

    when(userService.registerUser(any(User.class))).thenReturn(user);

    try {
      mockMvc
        .perform(
          post("/api/users/register")
            .contentType("application/json")
            .content(JsonUtils.toJson(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email", is("test@example.com")))
        .andExpect(jsonPath("$.name", is("Test User")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * GIven user with email test@example.com exists
   * When user registers with email test@example.com registers
   * Then bad request is returned
   */
  @Test
  public void testRegisterUserAlreadyExists_Failure() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setName("Test User");

    when(userService.registerUser(any(User.class))).thenThrow(
      new RuntimeException("User already exists")
    );
    try {
      mockMvc
        .perform(
          post("/api/users/register")
            .contentType("application/json")
            .content(JsonUtils.toJson(user))
        )
        .andExpect(status().isBadRequest());
    } catch (Exception e) {
      // Handle exception if needed
    }
  }

  /**
   * When request body is missing required fields (email, password, name)
   * Then bad request is returned
   */
  @Test
  public void testRegisterUser_MissingFields_Failure() {
    User user = new User();
    // Intentionally leaving out required fields

    try {
      mockMvc
        .perform(
          post("/api/users/register")
            .contentType("application/json")
            .content(JsonUtils.toJson(user))
        )
        .andExpect(status().isBadRequest());
    } catch (Exception e) {
      // Handle exception if needed
    }
  }

  // Should we try each field individually?

  /* FUNCTION public ResponseEntity<User> getUserByEmail(String email) */

  /* FUNCTION public ResponseEntity<User> login(LoginRequest loginRequest) */
  /**
   * Given an email that has a users associated
   * When user tries to login with that email and the correct passowrd
   * Then a status of 200 with the user is returned
   */
  @Test
  public void testLogin_Success() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setName("Test User");

    when(userService.login("test@example.com", "password123")).thenReturn(user);

    try {
      mockMvc
        .perform(
          post("/api/users/login")
            .contentType("application/json")
            .content(JsonUtils.toJson(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email", is("test@example.com")))
        .andExpect(jsonPath("$.name", is("Test User")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Given an email that has a users associated
   * When user tries to login with that email and an incorrect passowrd
   * a status of 401 is returned
   */
  @Test
  public void testLogin_IncorrectPassword_Failure() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setName("Test User");

    when(userService.login("test@example.com", "wrongPassword")).thenThrow(
      new RuntimeException("Invalid password")
    );
    try {
      mockMvc
        .perform(
          post("/api/users/login")
            .contentType("application/json")
            .content(JsonUtils.toJson(user))
        )
        .andExpect(status().isUnauthorized());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  /**
   * Given an email that has no users associated
   * When user tries to login with that email
   * a status of 401 is returned
   */
}

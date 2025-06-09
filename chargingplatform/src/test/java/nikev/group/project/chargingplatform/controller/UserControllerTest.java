package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Date;

import javax.crypto.SecretKey;

import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.TestMetricConfig;
import nikev.group.project.chargingplatform.model.Role;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.security.JwtTokenProvider;
import nikev.group.project.chargingplatform.service.UserService;

import org.flywaydb.core.internal.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.is;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(TestMetricConfig.class)
public class UserControllerTest {

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  private MockMvc mockMvc;

  @Value("${JWT_SECRET}")
  private String jwtSecret;

  public String getJwtForTestUser(){
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + 3600000);
    System.out.println("jwtSecret: " + jwtSecret);
    System.out.println("Bytes: " + jwtSecret.getBytes());
    SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    return Jwts.builder()
                .setSubject("test")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
  }

  /* FUNCTION public ResponseEntity<User> registerUser(User user) */
  /*
   * GIven no user with email test@example.com exists
   * When user registers with email test@example.com registers
   * Then user is created with email test@example.com
   */
  @Test
  public void whenRegisteringNewUser_thenUserIsCreated() {
    RegisterRequestDTO registerRequest = new RegisterRequestDTO();
    registerRequest.setEmail("test@example.com");
    registerRequest.setPassword("password123");
    registerRequest.setConfirmPassword("password123");
    registerRequest.setUsername("test");
    registerRequest.setAccountType("user");

    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setUsername("test");
    user.setRole(Role.USER);

    when(userService.registerUser(any(RegisterRequestDTO.class))).thenReturn(
      user
    );

    try {
      mockMvc
        .perform(
          post("/api/users/register")
            .contentType("application/json")
            .content(JsonUtils.toJson(registerRequest))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email", is("test@example.com")))
        .andExpect(jsonPath("$.username", is("test")));
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
  public void whenRegisteringExistingEmail_then400() {
    RegisterRequestDTO registerRequest = new RegisterRequestDTO();
    registerRequest.setEmail("test@example.com");
    registerRequest.setPassword("password123");
    registerRequest.setConfirmPassword("password123");
    registerRequest.setUsername("test");
    registerRequest.setAccountType("user");

    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setUsername("test");
    user.setRole(Role.USER);

    when(userService.registerUser(any(RegisterRequestDTO.class))).thenThrow(
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

  @Test
  public void whenRegisteringExistingUsername_then400(){
    RegisterRequestDTO registerRequest = new RegisterRequestDTO();
    registerRequest.setEmail("test@example.com");
    registerRequest.setPassword("password123");
    registerRequest.setConfirmPassword("password123");
    registerRequest.setUsername("test");
    registerRequest.setAccountType("user");

    User user = new User();
    user.setEmail("test2@example.com");
    user.setPassword("password123");
    user.setUsername("test");
    user.setRole(Role.USER);

    when(userService.registerUser(any(RegisterRequestDTO.class))).thenThrow(
      new RuntimeException("Username already exists")
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
  public void whenRequestBodyIsMissingRequiredFields_then400() {
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
  public void whenLoggingInWithValidCredentials_then200() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setUsername("test");
    user.setRole(Role.USER);

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
        .andExpect(jsonPath("$.username", is("test")));
    } catch (Exception e) {
      e.printStackTrace();
    }
    verify(userService, times(1)).login(anyString(), anyString());
  }

  /**
   * Given an email that has a users associated
   * When user tries to login with that email and an incorrect passowrd
   * a status of 401 is returned
   */
  @Test
  public void whenLoggingInWithInvalidCredentials_then401() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setUsername("Test User");
    user.setRole(Role.USER);

    when(userService.login(anyString(), anyString())).thenThrow(
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

    verify(userService, times(1)).login(anyString(), anyString());
  }

  /**
   * Given an email that has no users associated
   * When user tries to login with that email
   * a status of 401 is returned
   */
  @Test
  public void whenLoggingInWithInvalidEmail_then401() {
    when(userService.login(anyString(), anyString())).thenThrow(
      new RuntimeException("User not found")
    );

    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setRole(Role.USER);

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
    verify(userService, times(1)).login(anyString(), anyString());
  }

  @Test
  public void whenGetCurrentUserUnauthenticated_then401() throws Exception {
    // ensure no authentication is present
    SecurityContextHolder.clearContext();

    when(jwtTokenProvider.validateToken(anyString())).thenReturn(false);
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test", // The username, matching what getJwtForTestUser might imply
            null,
            Collections.singletonList(new SimpleGrantedAuthority("USER")) 
    );
    when(jwtTokenProvider.getAuthentication(anyString())).thenReturn(authentication);
    when(userService.getUserIdByUsername(eq("test"))).thenReturn(1L);

    mockMvc.perform(get("/api/users/me"))
           .andExpect(status().isForbidden());
  }

  @Test
  public void whenGetCurrentUserAuthenticated_then200() throws Exception {
    // 1) token is valid
    when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);

    // 2) prepare a real domain User as principal
    User user = new User();
    user.setId(1L);
    user.setEmail("me@example.com");
    user.setUsername("me");
    user.setRole(Role.USER);

    // 3) return that User in your Authentication
    Authentication authentication = new UsernamePasswordAuthenticationToken(
      user,
      null,
      Collections.singletonList(new SimpleGrantedAuthority("USER"))
    );
    when(jwtTokenProvider.getAuthentication(anyString()))
      .thenReturn(authentication);

    // 4) stub service calls your controller will make
    when(userService.getUserIdByUsername("me")).thenReturn(1L);
    when(userService.getUserByUsername("me")).thenReturn(user);

    // 5) generate a JWT so your filter sees a cookie
    String jwt = getJwtForTestUser();

    mockMvc.perform(get("/api/users/me")
            .cookie(new Cookie("JWT_TOKEN", jwt)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.email",   is("me@example.com")))
           .andExpect(jsonPath("$.username",is("me")));
  }
}

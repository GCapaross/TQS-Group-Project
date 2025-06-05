package nikev.group.project.chargingplatform.controller;

import lombok.Getter;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nikev.group.project.chargingplatform.DTOs.LoginRequestDTO;
import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserService userService;
  
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  private MeterRegistry meterRegistry;

  private final Counter userRegistrationCounter;
  private final Counter loginCounter;
  private final Timer registrationTimer;
  private final Timer loginTimer;

  public UserController(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    this.userRegistrationCounter = Counter.builder("app_users_registered_total")
        .description("Total number of user registrations")
        .tag("application", "chargingplatform")
        .register(meterRegistry);
    this.loginCounter = Counter.builder("app_logins_total")
        .description("Total number of login attempts")
        .tag("application", "chargingplatform")
        .register(meterRegistry);
    this.registrationTimer = Timer.builder("app_registration_latency")
        .description("User registration latency in seconds")
        .tag("application", "chargingplatform")
        .register(meterRegistry);
    this.loginTimer = Timer.builder("app_login_latency")
        .description("Login latency in seconds")
        .tag("application", "chargingplatform")
        .register(meterRegistry);
  }

  @PostMapping("/register")
  public ResponseEntity<User> registerUser(@RequestBody RegisterRequestDTO user) {
    userRegistrationCounter.increment();
    Timer.Sample sample = Timer.start(meterRegistry);
    
    try {
      if (
        user.getEmail() == null ||
        user.getPassword() == null ||
        user.getConfirmPassword() == null ||
        user.getUsername() == null ||
        user.getAccountType() == null ||
        !user.getPassword().equals(user.getConfirmPassword())
      ) {
        sample.stop(Timer.builder("app_registration_latency")
            .tag("status", "failure")
            .register(meterRegistry));
        return ResponseEntity.badRequest().build();
      }
      System.out.println("Registering user: " + user.getEmail());
      User registeredUser = userService.registerUser(user);
      sample.stop(Timer.builder("app_registration_latency")
          .tag("status", "success")
          .register(meterRegistry));
      return ResponseEntity.ok(registeredUser);
    } catch (RuntimeException e) {
      sample.stop(Timer.builder("app_registration_latency")
          .tag("status", "failure")
          .register(meterRegistry));
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<User> login(@RequestBody LoginRequestDTO loginRequest) {
    loginCounter.increment();
    Timer.Sample sample = Timer.start(meterRegistry);
    
    try {
      User user = userService.login(
        loginRequest.getEmail(),
        loginRequest.getPassword()
      );
      System.out.println("User logged in: " + user.getEmail());

      Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
      String token = jwtTokenProvider.generateToken(auth);

      ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
          .httpOnly(true)
          .secure(false)
          .path("/")
          .sameSite("Strict")
          .build();
      
      sample.stop(Timer.builder("app_login_latency")
          .tag("status", "success")
          .register(meterRegistry));
      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body(user);

    } catch (RuntimeException e) {
      sample.stop(Timer.builder("app_login_latency")
          .tag("status", "failure")
          .register(meterRegistry));
      System.out.println("Login failed: " + e.getMessage());
      return ResponseEntity.status(401).build();
    }
  }

  @GetMapping("/me")
  public ResponseEntity<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(401).build();
    }
    User user = (User) authentication.getPrincipal();
    return ResponseEntity.ok(user);
  }
}


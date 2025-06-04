package nikev.group.project.chargingplatform.controller;

import lombok.Getter;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nikev.group.project.chargingplatform.DTOs.LoginRequest;
import nikev.group.project.chargingplatform.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserService userService;
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @PostMapping("/register")
  public ResponseEntity<User> registerUser(@RequestBody User user) {
    if (
      user.getEmail() == null ||
      user.getPassword() == null ||
      user.getUsername() == null
    ) {
      return ResponseEntity.badRequest().build();
    }
    try {
      User registeredUser = userService.registerUser(user);
      return ResponseEntity.ok(registeredUser);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<User> login(@RequestBody LoginRequest loginRequest) {
    try {
      User user = userService.login(
        loginRequest.getEmail(),
        loginRequest.getPassword()
      );
      System.out.println("User logged in: " + user.getEmail());

      Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
      String token = jwtTokenProvider.generateToken(auth);
      System.out.println("Generated JWT token: " + token);

      ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
          .httpOnly(true)
          .secure(false)
          .path("/")
          .sameSite("Strict")
          .build();
      System.out.println("Set cookie: " + cookie.toString());
      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body(user);

    } catch (RuntimeException e) {
      System.out.println("Login failed: " + e.getMessage());
      return ResponseEntity.status(401).build();
    }
  }
}

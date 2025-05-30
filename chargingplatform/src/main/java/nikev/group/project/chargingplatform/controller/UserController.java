package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserService userService;

  /*
   * GIven no user with email test@example.com exists
   * When user registers with email text@example.com registers
   * Then user is created with email text@example.com
   */
  /*
   * GIven user with email test@example.com exists
   * When user registers with email text@example.com registers
   * Then bad request is returned
   */
  /**
   * When request body is missing required fields (email, password, name)
   * Then bad request is returned
   */
  @PostMapping("/register")
  public ResponseEntity<User> registerUser(@RequestBody User user) {
    if (
      user.getEmail() == null ||
      user.getPassword() == null ||
      user.getName() == null
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

  /**
   * Given an email that has a users associated
   * When user tries to login with that email and the correct passowrd
   * Then a status of 200 with the user is returned
   */
  /**
   * Given an email that has a users associated
   * When user tries to login with that email and an incorrect passowrd
   * a status of 401 is returned
   */
  /**
   * Given an email that has no users associated
   * When user tries to login with that email
   * a status of 401 is returned
   */
  @PostMapping("/login")
  public ResponseEntity<User> login(@RequestBody LoginRequest loginRequest) {
    try {
      User user = userService.login(
        loginRequest.getEmail(),
        loginRequest.getPassword()
      );
      return ResponseEntity.ok(user);
    } catch (RuntimeException e) {
      return ResponseEntity.status(401).build();
    }
  }

  /**
   * Given a user with id 1
   * When user with id 1 tries to update their profile with valid data
   * Then a status of 200 with the updated user is returned
   */
  /**
   * Given a user with id 1 with email old@example.com
   *  and another user with id 2 with email test@example.com
   * When user with id 1 tries to update their email to test@example.com
   * Then a status of 400 is returned
   */
  /**
   * Given a user with id 1 with email test@example.com
   * When user with id 1 tries to update profile of unexistent user (id -1)
   * Then a status of 404 is returned
   */
  @PutMapping("/{id}")
  public ResponseEntity<User> updateProfile(
    @PathVariable Long id,
    @RequestBody User updatedUser
  ) {
    try {
      User user = userService.updateProfile(id, updatedUser);
      return ResponseEntity.ok(user);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  private static class LoginRequest {

    private String email;
    private String password;

    public String getEmail() {
      return email;
    }

    // public void setEmail(String email) {
    //     this.email = email;
    // }

    public String getPassword() {
      return password;
    }
    // public void setPassword(String password) {
    //     this.password = password;
    // }
  }
}

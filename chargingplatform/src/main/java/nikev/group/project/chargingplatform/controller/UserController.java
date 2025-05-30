package nikev.group.project.chargingplatform.controller;

import lombok.Getter;
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

  // @PutMapping("/{id}")
  // public ResponseEntity<User> updateProfile(
  //   @PathVariable Long id,
  //   @RequestBody User updatedUser
  // ) {
  //   try {
  //     User user = userService.updateProfile(id, updatedUser);
  //     return ResponseEntity.ok(user);
  //   } catch (RuntimeException e) {
  //     return ResponseEntity.notFound().build();
  //   }
  // }
  @Getter
  private static class LoginRequest {

    private String email;
    private String password;
  }
}

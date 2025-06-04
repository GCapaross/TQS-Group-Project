package nikev.group.project.chargingplatform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User testUser;

  private RegisterRequestDTO registerRequestDTO;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setPassword("password123");
    testUser.setUsername("Test User");

    registerRequestDTO = new RegisterRequestDTO();
    registerRequestDTO.setEmail(testUser.getEmail());
    registerRequestDTO.setPassword(testUser.getPassword());
    registerRequestDTO.setConfirmPassword(testUser.getPassword());
    registerRequestDTO.setUsername(testUser.getUsername());
    registerRequestDTO.setAccountType("user");
  }

  /**
   * Given no user with the email test@example.com exists
   * When a new user tries to register with the email test@example.com
   * Then a new User with email test@example.com is created
   */
  @Test
  void whenRegisteringNewUser_thenUserIsCreated() {
    // Arrange
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

    // Act
    User registeredUser = userService.registerUser(registerRequestDTO);

    // Assert
    assertNotNull(registeredUser);
    assertEquals(testUser.getEmail(), registeredUser.getEmail());
    assertEquals(testUser.getUsername(), registeredUser.getUsername());
    verify(userRepository).save(any(User.class));
  }

  /**
   * Given a User with email test@example.com
   * When an another user tries to register with the email test@example.com
   * Then RuntimeException is thrown
   */
  @Test
  void whenRegisteringExistingEmail_thenThrowsException() {
    // Arrange
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      userService.registerUser(registerRequestDTO)
    );
  }

  /**
   * Given an email that has a users associated
   * When user tries to login with that email and the correct passowrd
   * Then the user associated with that email is returned
   */
  @Test
  void whenLoggingInWithValidCredentials_thenReturnsUser() {
    // Arrange
    when(userRepository.findByEmail(testUser.getEmail())).thenReturn(
      Optional.of(testUser)
    );

    // Act
    User loggedInUser = userService.login(
      testUser.getEmail(),
      testUser.getPassword()
    );

    // Assert
    assertNotNull(loggedInUser);
    assertEquals(testUser.getEmail(), loggedInUser.getEmail());
  }

  /**
   * Given an email that has a users associated
   * When user tries to login with that email and an incorrect passowrd
   * Then RuntimeException with the message "Invalid passowrd" is thrown
   */
  @Test
  void whenLoggingInWithInvalidCredentials_thenThrowsException() {
    // Arrange
    when(userRepository.findByEmail(testUser.getEmail())).thenReturn(
      Optional.of(testUser)
    );

    User loginUser = new User();
    loginUser.setEmail(testUser.getEmail());
    loginUser.setPassword("IncorrectPassword");
    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      userService.login(loginUser.getEmail(), "wrongPassword")
    );
  }

  /**
   * Given an email that has no users associated
   * When user tries to login with that email
   * Then RuntimeException with the message "User not found" is thrown
   */
  @Test
  void whenLoggingInWithInvalidEmail_thenThrowsException() {
    // Arrange
    when(userRepository.findByEmail(testUser.getEmail())).thenReturn(
      Optional.empty()
    );

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      userService.login(testUser.getEmail(), "password")
    );
  }
}

package nikev.group.project.chargingplatform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.model.Role;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionSystemException;

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
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      userService.registerUser(registerRequestDTO)
    );
    assertEquals("Email already registered", exception.getMessage());
  }

  /**
   * Given a User with username "Test User"
   * When an another user tries to register with the username "Test User"
   * Then RuntimeException is thrown
   */
  @Test
  void whenRegisteringExistingUsername_thenThrowsException() {
    // Arrange
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(userRepository.findByUsername(testUser.getUsername())).thenReturn(
      Optional.of(testUser)
    );

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      userService.registerUser(registerRequestDTO)
    );
    assertEquals("Username already taken", exception.getMessage());
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

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      userService.login(testUser.getEmail(), "wrongPassword")
    );
    assertEquals("Invalid password", exception.getMessage());
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

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      userService.login(testUser.getEmail(), "password")
    );
    assertEquals("User not found", exception.getMessage());
  }

  @Test
  void whenRegisteringOperator_thenUserIsCreatedWithOperatorType() {
    // Arrange
    registerRequestDTO.setAccountType("operator");
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    User registeredUser = userService.registerUser(registerRequestDTO);

    // Assert
    assertNotNull(registeredUser);
    assertEquals(registerRequestDTO.getEmail(), registeredUser.getEmail());
    assertEquals(registerRequestDTO.getUsername(), registeredUser.getUsername());
    assertEquals(Role.OPERATOR, registeredUser.getRole());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void whenGettingExistingUserByUsername_thenReturnsUser() {
    // Arrange
    when(userRepository.findByUsername(testUser.getUsername()))
      .thenReturn(Optional.of(testUser));

    // Act
    User found = userService.getUserByUsername(testUser.getUsername());

    // Assert
    assertNotNull(found);
    assertEquals(testUser.getEmail(), found.getEmail());
    assertEquals(testUser.getUsername(), found.getUsername());
    verify(userRepository).findByUsername(testUser.getUsername());
  }

  @Test
  void whenGettingNonexistentUserByUsername_thenThrowsException() {
    // Arrange
    String missingUsername = "missing";
    when(userRepository.findByUsername(missingUsername))
      .thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      userService.getUserByUsername(missingUsername)
    );
    assertEquals("User not found", exception.getMessage());
    verify(userRepository).findByUsername(missingUsername);
  }

  @Test
  void whenGettingUserIdByUsername_thenReturnsId() {
    // Arrange
    when(userRepository.findByUsername(testUser.getUsername()))
      .thenReturn(Optional.of(testUser));

    // Act
    Long userId = userService.getUserIdByUsername(testUser.getUsername());

    // Assert
    assertEquals(testUser.getId(), userId);
    verify(userRepository).findByUsername(testUser.getUsername());
  }

  @Test
  void whenGettingUserIdByNonexistentUsername_thenThrowsException() {
    // Arrange
    String missingUsername = "missing";
    when(userRepository.findByUsername(missingUsername))
      .thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      userService.getUserIdByUsername(missingUsername)
    );
    assertEquals("User not found", exception.getMessage());
    verify(userRepository).findByUsername(missingUsername);
  }


  @Test
  void whenRepositoryThrowsException_thenExceptionIsPropagated() {
    // Arrange
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenThrow(new TransactionSystemException("Database error"));

    // Act & Assert
    assertThrows(TransactionSystemException.class, () ->
      userService.registerUser(registerRequestDTO)
    );
  }
}
package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setName("Test User");
    }

    @Test
    void whenRegisteringNewUser_thenUserIsCreated() {
        // Arrange
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User registeredUser = userService.registerUser(testUser);

        // Assert
        assertNotNull(registeredUser);
        assertEquals(testUser.getEmail(), registeredUser.getEmail());
        assertEquals(testUser.getName(), registeredUser.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenRegisteringExistingEmail_thenThrowsException() {
        // Arrange
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userService.registerUser(testUser)
        );
    }

    @Test
    void whenLoggingInWithValidCredentials_thenReturnsUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        // Act
        User loggedInUser = userService.login(testUser.getEmail(), testUser.getPassword());

        // Assert
        assertNotNull(loggedInUser);
        assertEquals(testUser.getEmail(), loggedInUser.getEmail());
    }

    @Test
    void whenLoggingInWithInvalidCredentials_thenThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userService.login(testUser.getEmail(), "wrongPassword")
        );
    }

    @Test
    void whenUpdatingUserProfile_thenProfileIsUpdated() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        User updatedUser = new User();
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        // Act
        User result = userService.updateProfile(testUser.getId(), updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals(updatedUser.getName(), result.getName());
        assertEquals(updatedUser.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenUpdatingNonExistentUser_thenThrowsException() {
        // Arrange
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userService.updateProfile(999L, testUser)
        );
    }
} 
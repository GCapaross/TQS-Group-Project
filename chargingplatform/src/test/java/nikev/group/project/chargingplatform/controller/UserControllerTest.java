package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import nikev.group.project.chargingplatform.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@WebMvcTest(UserController.class)
public class UserControllerTest {

  @MockitoBean
  private UserService userService;
  /* FUNCTION public ResponseEntity<User> registerUser(User user) */
  /*
   * GIven no user with email test@example.com exists
   * When user registers with email test@example.com registers
   * Then user is created with email test@example.com
   */

  /*
   * GIven user with email test@example.com exists
   * When user registers with email test@example.com registers
   * Then bad request is returned
   */
  /**
   * When request body is missing required fields (email, password, name)
   * Then bad request is returned
   */

  /* FUNCTION public ResponseEntity<User> login(LoginRequest loginRequest) */
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
}

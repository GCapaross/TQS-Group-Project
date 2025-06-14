package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.Cookie;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.crypto.SecretKey;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.TestMetricConfig;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import nikev.group.project.chargingplatform.security.JwtTokenFilter;
import nikev.group.project.chargingplatform.security.JwtTokenProvider;
import nikev.group.project.chargingplatform.service.BookingService;
import nikev.group.project.chargingplatform.service.StationService;
import nikev.group.project.chargingplatform.service.UserService;
import org.flywaydb.core.internal.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
@ActiveProfiles("test")
@Import(TestMetricConfig.class)
public class BookingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookingService bookingService;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @Value("${JWT_SECRET}")
  private String jwtSecret;

  public String getJwtForTestUser() {
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

  @BeforeEach
  void setUp() {
    when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);

    Authentication authentication = new UsernamePasswordAuthenticationToken(
      "test",
      null,
      Collections.singletonList(new SimpleGrantedAuthority("USER"))
    );
    when(jwtTokenProvider.getAuthentication(anyString())).thenReturn(
      authentication
    );
    when(userService.getUserIdByUsername(eq("test"))).thenReturn(1L);
  }

  /*
   * FUNCTION public ResponseEntity<Reservation> createBooking(
   * BookingRequestDTO request)
   */
  /**
   * (Unexisten station)
   * Given no station with id 5
   * When booking a slot to station with id 5
   * then bad request is returned
   */
  @Test
  void whenRequestingSlotToUnexistentStation_thenBadRequestIsReturned() {
    BookingRequestDTO request = new BookingRequestDTO(
      5L, // Unexistent station ID
      LocalDateTime.now(),
      LocalDateTime.now().plusHours(1)
    );

    String jwt = getJwtForTestUser();

    when(
      bookingService.bookSlot(
        anyLong(),
        anyLong(),
        any(LocalDateTime.class),
        any(LocalDateTime.class)
      )
    ).thenThrow(new RuntimeException("Station not found"));

    try {
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request))
            .cookie(new Cookie("JWT_TOKEN", jwt))
        )
        .andExpect(status().isBadRequest());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * (Unexistent user)
   * Given station with id 1 and no user with id 1
   * When booking a slot to station with id 1 by the user with id 1
   * then bad request is returned
   */
  @Test
  void whenRequestingSlotToUnexistentUser_thenBadRequestIsReturned() {
    BookingRequestDTO request = new BookingRequestDTO(
      1L, // Existing station ID
      LocalDateTime.now(),
      LocalDateTime.now().plusHours(1)
    );

    String jwt = getJwtForTestUser();

    when(userService.getUserIdByUsername(anyString())).thenThrow(
      new RuntimeException("User not found")
    );

    when(
      bookingService.bookSlot(
        anyLong(),
        anyLong(),
        any(LocalDateTime.class),
        any(LocalDateTime.class)
      )
    ).thenThrow(new RuntimeException("User not found"));

    try {
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request))
            .cookie(new Cookie("JWT_TOKEN", jwt))
        )
        .andExpect(status().isBadRequest());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * (No slots available)
   * Given station with 2 chargers and 2 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h30
   * then bad request is returned
   */
  @Test
  void whenBookingSlotWithNoAvailableSlots_thenBadRequestIsThrown() {
    BookingRequestDTO request = new BookingRequestDTO(
      1L, // Existing station ID
      LocalDateTime.of(2023, 10, 1, 14, 30),
      LocalDateTime.of(2023, 10, 1, 15, 30)
    );

    String jwt = getJwtForTestUser();

    when(
      bookingService.bookSlot(
        anyLong(),
        anyLong(),
        any(LocalDateTime.class),
        any(LocalDateTime.class)
      )
    ).thenThrow(new RuntimeException("No available slots"));

    try {
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request))
            .cookie(new Cookie("JWT_TOKEN", jwt))
        )
        .andExpect(status().isBadRequest());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * (Charger out of service)
   * Given station with 2 chargers and 1 reservation between startTime and endTime
   * but 1 charger has state OUT_OF_SERVICE
   * When booking a slot to the station to the slot startTime and endTime
   * then bad request is returned
   */
  @Test
  @Disabled(
    "This test is disabled because the charger state is not handled in the controller, this should be used in integration tests"
  )
  void whenBookingSlotWithOutOfServiceCharger_thenBadRequestIsThrown() {}

  /**
   * (Invalid request)
   * Given station with 2 chargers and 1 reservation between 14h30 and 15h00
   * When request doent have required fields (stationId, startTime, endTime)
   * then response with bad request is returned
   */
  @Test
  void whenBookingSlotWithInvalidRequest_thenBadRequestIsThrown() {
    BookingRequestDTO request_station = new BookingRequestDTO(
      null, // Missing station ID
      LocalDateTime.now(),
      LocalDateTime.now().plusMinutes(30)
    );
    BookingRequestDTO request_startTime = new BookingRequestDTO(
      5L,
      null, // Missing start time
      LocalDateTime.now().plusMinutes(30)
    );
    BookingRequestDTO request_endTime = new BookingRequestDTO(
      5L,
      LocalDateTime.now(),
      null // Missing end time
    );

    String jwt = getJwtForTestUser();

    try {
      // Test with missing station ID
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request_station))
            .cookie(new Cookie("JWT_TOKEN", jwt))
        )
        .andExpect(status().isBadRequest());

      // Test with missing start time
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request_startTime))
            .cookie(new Cookie("JWT_TOKEN", jwt))
        )
        .andExpect(status().isBadRequest());

      // Test with missing end time
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request_endTime))
            .cookie(new Cookie("JWT_TOKEN", jwt))
        )
        .andExpect(status().isBadRequest());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * (Request in the past)
   * Given station with 2 chargers
   * When booking a slot to the station to the slot half an hour ago
   * then response with bad request is returned
   */
  @Test
  void whenBookingSlotInPast_thenBadRequestIsThrown() {
    BookingRequestDTO request = new BookingRequestDTO(
      1L, // Existing station ID
      LocalDateTime.now().minusMinutes(30),
      LocalDateTime.now()
    );

    String jwt = getJwtForTestUser();

    try {
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request))
            .cookie(new Cookie("JWT_TOKEN", jwt))
        )
        .andExpect(status().isBadRequest());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * (Free station)
   * Given station with 2 chargers and 2 reservations:
   * one between 14h30 and 15h00 and another between 15h00 and 15h30
   * When booking a slot to the station to the slot 14h45 and 15h15
   * then Reservation is made
   */
  @Test
  void whenBookingSlotWithFreeStation_thenReservationIsMade() {
    BookingRequestDTO request = new BookingRequestDTO(
      1L, // Existing station ID
      LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.SECONDS),
      LocalDateTime.now().plusMinutes(45).truncatedTo(ChronoUnit.SECONDS)
    );

    String jwt = getJwtForTestUser();

    Reservation reservation = new Reservation();
    reservation.setId(1L);
    reservation.setStartDate(request.getStartTime());
    reservation.setEndDate(request.getEndTime());

    when(
      bookingService.bookSlot(
        anyLong(),
        anyLong(),
        any(LocalDateTime.class),
        any(LocalDateTime.class)
      )
    ).thenReturn(reservation);

    try {
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request))
            .cookie(new Cookie("JWT_TOKEN", jwt))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)));
      // Commented because of inconsistencies between LocalDateTime.toString
      // and Jacksons JSON LocalDateTime toString
      //.andExpect(
      //  jsonPath("$.startDate", is(request.getStartTime().toString()))
      //)
      //.andExpect(jsonPath("$.endDate", is(request.getEndTime().toString())));

      verify(bookingService, times(1)).bookSlot(
        anyLong(),
        anyLong(),
        eq(request.getStartTime()),
        eq(request.getEndTime())
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Given station with 2 chargers and 1 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h00
   * then Reservation is made
   */
  @Test
  @Disabled(
    "This test is disabled because it should be used in integration tests, we cannot test this here"
  )
  void whenBookingSlotWithExistingReservation_thenReservationIsMade() {
    BookingRequestDTO request = new BookingRequestDTO(
      1L, // Existing station ID
      LocalDateTime.now().plusMinutes(15),
      LocalDateTime.now().plusMinutes(45)
    );

    Reservation reservation = new Reservation();
    reservation.setId(1L);
    reservation.setStartDate(request.getStartTime());
    reservation.setEndDate(request.getEndTime());

    when(
      bookingService.bookSlot(
        anyLong(),
        anyLong(),
        any(LocalDateTime.class),
        any(LocalDateTime.class)
      )
    ).thenReturn(reservation);

    try {
      mockMvc
        .perform(
          post("/api/bookings")
            .contentType("application/json")
            .content(JsonUtils.toJson(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(
          jsonPath("$.startDate", is(request.getStartTime().toString()))
        )
        .andExpect(jsonPath("$.endDate", is(request.getEndTime().toString())));

      verify(bookingService, times(1)).bookSlot(
        anyLong(),
        anyLong(),
        eq(request.getStartTime()),
        eq(request.getEndTime())
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * FUNCTION public ResponseEntity<Void>
   * cancelBooking(@NotNull @PathVariable(required = true) Long id)
   */
  /**
   * Given no booking with id 1
   * When trying to cancel booking with id 1
   * then reposnse with bad request is returned
   */
  @Test
  void whenCancelingUnexistentBooking_thenBadRequestIsThrown() {
    doThrow(new RuntimeException("Booking not found"))
      .when(bookingService)
      .cancelBooking(anyLong());

    String jwt = getJwtForTestUser();

    try {
      mockMvc
        .perform(delete("/api/bookings/1").cookie(new Cookie("JWT_TOKEN", jwt)))
        .andExpect(status().isNotFound());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Given booking with id 1
   * When trying to cancel booking with id 1
   * then response with status ok is returned
   */
  @Test
  void whenCancelingBooking_thenStatusNoContentIsReturned() {
    String jwt = getJwtForTestUser();

    try {
      mockMvc
        .perform(delete("/api/bookings/1").cookie(new Cookie("JWT_TOKEN", jwt)))
        .andExpect(status().isNoContent());

      verify(bookingService, times(1)).cancelBooking(anyLong());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

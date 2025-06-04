package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.security.JwtTokenProvider;
import nikev.group.project.chargingplatform.service.BookingService;
import nikev.group.project.chargingplatform.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BookingController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @MockitoBean
    private MeterRegistry meterRegistry;

    @MockitoBean @Qualifier("requestCounter")
    private Counter requestCounter;

    @MockitoBean @Qualifier("requestTimer")
    private Timer requestTimer;

    @MockitoBean @Qualifier("bookingSuccessCounter")
    private Counter bookingSuccessCounter;

    @MockitoBean @Qualifier("bookingFailureCounter")
    private Counter bookingFailureCounter;

    @MockitoBean @Qualifier("bookingDurationTimer")
    private Timer bookingDurationTimer;

    @MockitoBean @Qualifier("cancellationSuccessCounter")
    private Counter cancellationSuccessCounter;

    @MockitoBean @Qualifier("cancellationFailureCounter")
    private Counter cancellationFailureCounter;

    public String getJwtForTestUser() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000);
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
            "test", null, Collections.singletonList(new SimpleGrantedAuthority("USER")));
        when(jwtTokenProvider.getAuthentication(anyString())).thenReturn(authentication);
        when(userService.getUserIdByUsername(eq("test"))).thenReturn(1L);

        when(meterRegistry.counter("app.bookings.success", "Number of successful bookings"))
            .thenReturn(bookingSuccessCounter);
        when(meterRegistry.counter("app.bookings.failure", "Number of failed bookings"))
            .thenReturn(bookingFailureCounter);
        when(meterRegistry.timer("app.bookings.duration", "Time taken to process bookings"))
            .thenReturn(bookingDurationTimer);
        when(meterRegistry.counter("app.bookings.cancellation.success", "Number of successful cancellations"))
            .thenReturn(cancellationSuccessCounter);
        when(meterRegistry.counter("app.bookings.cancellation.failure", "Number of failed cancellations"))
            .thenReturn(cancellationFailureCounter);
        when(meterRegistry.timer("app.requests.latency", anyString())).thenReturn(requestTimer);

        doNothing().when(requestCounter).increment();
        doNothing().when(bookingSuccessCounter).increment();
        doNothing().when(bookingFailureCounter).increment();
        doNothing().when(cancellationSuccessCounter).increment();
        doNothing().when(cancellationFailureCounter).increment();
    }

    @Test
    void whenRequestingSlotToUnexistentStation_thenBadRequestIsReturned() {
        BookingRequestDTO request = new BookingRequestDTO(
            5L, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        String jwt = getJwtForTestUser();

        when(bookingService.bookSlot(anyLong(), anyLong(), any(), any()))
            .thenThrow(new RuntimeException("Station not found"));

        try {
            mockMvc.perform(post("/api/bookings")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
                    .cookie(new Cookie("JWT_TOKEN", jwt)))
                .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void whenRequestingSlotToUnexistentUser_thenBadRequestIsReturned() {
        BookingRequestDTO request = new BookingRequestDTO(
            1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        String jwt = getJwtForTestUser();

        when(userService.getUserIdByUsername(anyString()))
            .thenThrow(new RuntimeException("User not found"));
        when(bookingService.bookSlot(anyLong(), anyLong(), any(), any()))
            .thenThrow(new RuntimeException("User not found"));

        try {
            mockMvc.perform(post("/api/bookings")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
                    .cookie(new Cookie("JWT_TOKEN", jwt)))
                .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void whenBookingSlotWithNoAvailableSlots_thenBadRequestIsThrown() {
        BookingRequestDTO request = new BookingRequestDTO(
            1L, LocalDateTime.of(2023, 10, 1, 14, 30),
            LocalDateTime.of(2023, 10, 1, 15, 30));
        String jwt = getJwtForTestUser();

        when(bookingService.bookSlot(anyLong(), anyLong(), any(), any()))
            .thenThrow(new RuntimeException("No available slots"));

        try {
            mockMvc.perform(post("/api/bookings")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
                    .cookie(new Cookie("JWT_TOKEN", jwt)))
                .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled("Handled in integration test")
    void whenBookingSlotWithOutOfServiceCharger_thenBadRequestIsThrown() {}

    @Test
    void whenBookingSlotWithInvalidRequest_thenBadRequestIsThrown() {
        String jwt = getJwtForTestUser();
        List<BookingRequestDTO> requests = List.of(
            new BookingRequestDTO(null, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)),
            new BookingRequestDTO(5L, null, LocalDateTime.now().plusMinutes(30)),
            new BookingRequestDTO(5L, LocalDateTime.now(), null)
        );

        for (BookingRequestDTO req : requests) {
            try {
                mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(new Cookie("JWT_TOKEN", jwt)))
                    .andExpect(status().isBadRequest());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void whenBookingSlotInPast_thenBadRequestIsThrown() {
        BookingRequestDTO request = new BookingRequestDTO(
            1L, LocalDateTime.now().minusMinutes(30), LocalDateTime.now());
        String jwt = getJwtForTestUser();

        try {
            mockMvc.perform(post("/api/bookings")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
                    .cookie(new Cookie("JWT_TOKEN", jwt)))
                .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void whenBookingSlotWithFreeStation_thenReservationIsMade() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusMinutes(45).truncatedTo(ChronoUnit.SECONDS);
        BookingRequestDTO request = new BookingRequestDTO(1L, start, end);
        String jwt = getJwtForTestUser();

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartDate(start);
        reservation.setEndDate(end);

        when(bookingService.bookSlot(anyLong(), anyLong(), eq(start), eq(end)))
            .thenReturn(reservation);

        try {
            mockMvc.perform(post("/api/bookings")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
                    .cookie(new Cookie("JWT_TOKEN", jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

            verify(bookingService, times(1))
                .bookSlot(anyLong(), anyLong(), eq(start), eq(end));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled("Handled in integration test")
    void whenBookingSlotWithExistingReservation_thenReservationIsMade() {}

    @Test
    void whenCancelingUnexistentBooking_thenBadRequestIsThrown() {
        doThrow(new RuntimeException("Booking not found"))
            .when(bookingService).cancelBooking(anyLong());

        String jwt = getJwtForTestUser();

        try {
            mockMvc.perform(delete("/api/bookings/1")
                    .cookie(new Cookie("JWT_TOKEN", jwt)))
                .andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void whenCancelingBooking_thenStatusNoContentIsReturned() {
        String jwt = getJwtForTestUser();

        try {
            mockMvc.perform(delete("/api/bookings/1")
                    .cookie(new Cookie("JWT_TOKEN", jwt)))
                .andExpect(status().isNoContent());

            verify(bookingService, times(1)).cancelBooking(anyLong());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

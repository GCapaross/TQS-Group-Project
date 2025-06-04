package nikev.group.project.chargingplatform.controller;

import jakarta.validation.constraints.NotNull;

import java.security.Security;
import java.time.LocalDateTime;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.service.BookingService;
import nikev.group.project.chargingplatform.service.UserService;
import nikev.group.project.chargingplatform.controller.UserController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication; 

import jakarta.validation.constraints.NotNull;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Qualifier;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
  private static final Logger log = LoggerFactory.getLogger(BookingController.class);
  @Autowired
  private BookingService bookingService;

  @Autowired
  private UserService userService;

  @Autowired
  private MeterRegistry meterRegistry;

  @Autowired
  @Qualifier("requestCounter")
  private Counter requestCounter;

  @Autowired
  @Qualifier("requestTimer")
  private Timer requestTimer;

  private final Counter bookingSuccessCounter;
  private final Counter bookingFailureCounter;
  private final Timer bookingDurationTimer;
  private final Counter cancellationSuccessCounter;
  private final Counter cancellationFailureCounter;

  public BookingController(MeterRegistry meterRegistry) {
    this.bookingSuccessCounter = Counter.builder("app.bookings.success")
        .description("Number of successful bookings")
        .register(meterRegistry);
    this.bookingFailureCounter = Counter.builder("app.bookings.failure")
        .description("Number of failed bookings")
        .register(meterRegistry);
    this.bookingDurationTimer = Timer.builder("app.bookings.duration")
        .description("Time taken to process bookings")
        .register(meterRegistry);
    this.cancellationSuccessCounter = Counter.builder("app.bookings.cancellation.success")
        .description("Number of successful cancellations")
        .register(meterRegistry);
    this.cancellationFailureCounter = Counter.builder("app.bookings.cancellation.failure")
        .description("Number of failed cancellations")
        .register(meterRegistry);
  }

  @PostMapping
  public ResponseEntity<Reservation> createBooking(
    @RequestBody(required = true) BookingRequestDTO request
  ) {
    requestCounter.increment();
    Timer.Sample sample = Timer.start();
    try {
      if (!isValidBookingRequest(request)) {
        bookingFailureCounter.increment();
        return ResponseEntity.badRequest().build();
      }

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication.getName();
      log.info("Authenticated user: {}", username);
      Long userId = userService.getUserIdByUsername(username);
      log.info("User ID for {}: {}", username, userId);

      Reservation session = bookingService.bookSlot(
        request.getStationId(),
        userId,
        request.getStartTime(),
        request.getEndTime()
      );

      bookingSuccessCounter.increment();
      sample.stop(Timer.builder("app.requests.latency")
          .tag("endpoint", "createBooking")
          .tag("status", "success")
          .register(meterRegistry));
      return ResponseEntity.ok(session);
    } catch (RuntimeException e) {
      bookingFailureCounter.increment();
      sample.stop(Timer.builder("app.requests.latency")
          .tag("endpoint", "createBooking")
          .tag("status", "failure")
          .register(meterRegistry));
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancelBooking(
    @NotNull @PathVariable(required = true) Long id
  ) {
    requestCounter.increment();
    Timer.Sample sample = Timer.start();
    try {
      bookingService.cancelBooking(id);
      cancellationSuccessCounter.increment();
      sample.stop(Timer.builder("app.requests.latency")
          .tag("endpoint", "cancelBooking")
          .tag("status", "success")
          .register(meterRegistry));
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      cancellationFailureCounter.increment();
      sample.stop(Timer.builder("app.requests.latency")
          .tag("endpoint", "cancelBooking")
          .tag("status", "failure")
          .register(meterRegistry));
      return ResponseEntity.notFound().build();
    }
  }

  private boolean isValidBookingRequest(BookingRequestDTO request) {
    if (
      request.getStationId() == null ||
      request.getStartTime() == null ||
      request.getEndTime() == null
    ) {

      return false;
    }

    LocalDateTime now = LocalDateTime.now();

    if (request.getStartTime().isBefore(now)) {
      return false;
    }

    if (request.getEndTime().isBefore(request.getStartTime())) {
      return false;
    }

    return true;
  }
}

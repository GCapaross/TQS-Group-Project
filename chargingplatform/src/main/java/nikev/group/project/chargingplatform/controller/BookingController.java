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

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
  private static final Logger log = LoggerFactory.getLogger(BookingController.class);
  @Autowired
  private BookingService bookingService;

  @Autowired
  private UserService userService;

  @PostMapping
  public ResponseEntity<Reservation> createBooking(
    @RequestBody(required = true) BookingRequestDTO request
  ) {
    try {
      if (!isValidBookingRequest(request)) {
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

      return ResponseEntity.ok(session);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancelBooking(
    @NotNull @PathVariable(required = true) Long id
  ) {
    try {
      bookingService.cancelBooking(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
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

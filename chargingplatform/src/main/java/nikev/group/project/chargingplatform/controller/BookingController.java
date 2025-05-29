package nikev.group.project.chargingplatform.controller;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  @Autowired
  private BookingService bookingService;

  /**
   * (Unexisten station)
   * Given no station with id 5
   * When booking a slot to station with id 5
   * then throw RuntimeException
   */
  /**
   * (Unexistent user)
   * Given station with id 1 and no user with id 1
   * When booking a slot to station with id 1 by the user with id 1
   * then throw RuntimeException
   */
  /**
   * (No slots available)
   * Given station with 2 chargers and 2 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h30
   * then throw RuntimeException
   */
  /**
   * (Charger out of service)
   * Given station with 2 chargers and 1 reservation between startTime and endTime
   * but 1 charger has state OUT_OF_SERVICE
   * When booking a slot to the station to the slot startTime and endTime
   * then throw RuntimeException
   */
  /**
   * (Free station)
   * Given station with 2 chargers and 2 reservations:
   * one between 14h30 and 15h00 and another between 15h00 and 15h30
   * When booking a slot to the station to the slot 14h45 and 15h15
   * then Reservation is made
   */
  /**
   * Given station with 2 chargers and 1 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h00
   * then Reservation is made
   */
  @PostMapping
  public ResponseEntity<Reservation> createBooking(
    @RequestBody(required = true) BookingRequestDTO request
  ) {
    try {
      if (!isValidBookingRequest(request)) {
        return ResponseEntity.badRequest().build();
      }

      Long userId = 1L;

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

  /**
   * Given no booking with id 1
   * When trying to cancel booking with id 1
   * then reposnse with bad request is returned
   */
  /**
   * Given booking with id 1
   * When trying to cancel booking with id 1
   * then response with status ok is returned
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancelBooking(
    @NotNull @PathVariable(required = true) Long id
  ) {
    try {
      bookingService.cancelBooking(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
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
  // private Long extractUserIdFromToken(String token) {
  //     // TODO: Implement proper JWT token validation and user ID extraction
  //     // For now, return a default user ID for testing
  //     return 1L;
  // }

}

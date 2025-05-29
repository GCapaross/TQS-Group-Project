package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<Reservation> createBooking(
        @NotNull @RequestBody(required=true) BookingRequestDTO request
    ) {
        try {
            log.info("BookingRequest: start={} end={}", request.getStartTime(), request.getEndTime());
            if (!isValidBookingRequest(request)) {
                log.warn("BookingRequest rejected by validation");
                return ResponseEntity.badRequest().build();
            }

            Long userId = 1L;

            Reservation session = bookingService.bookSlot(
                    request.getStationId(),
                    userId,
                    request.getStartTime(),
                    request.getEndTime());

            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @NotNull @PathVariable(required=true) Long id) {
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private boolean isValidBookingRequest(BookingRequestDTO request) {
        if (request.getStationId() == null ||
                request.getStartTime() == null ||
                request.getEndTime() == null) {
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
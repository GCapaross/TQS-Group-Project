package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<ChargingSession> createBooking(
        @NotNull @RequestBody(required=true) BookingRequestDTO request
    ) {
        try {
            if (!isValidBookingRequest(request)) {
                return ResponseEntity.badRequest().build();
            }

            Long userId = 1L;

            ChargingSession session = bookingService.bookSlot(
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

    private Long extractUserIdFromToken(String token) {
        // TODO: Implement proper JWT token validation and user ID extraction
        // For now, return a default user ID for testing
        return 1L;
    }

}
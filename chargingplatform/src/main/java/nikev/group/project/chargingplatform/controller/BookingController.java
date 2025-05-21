package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<ChargingSession> createBooking(
            @RequestHeader("Authorization") String token,
            @RequestBody BookingRequest request) {
        try {
            // Validate token
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().build();
            }

            // Validate request
            if (request == null || !isValidBookingRequest(request)) {
                return ResponseEntity.badRequest().build();
            }

            // Extract user ID from token
            Long userId = extractUserIdFromToken(token);
            
            ChargingSession session = bookingService.bookSlot(
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
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // Validate token
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().build();
            }

            // Validate booking ID
            if (id == null) {
                return ResponseEntity.badRequest().build();
            }

            // Extract user ID from token
            Long userId = extractUserIdFromToken(token);
            
            bookingService.cancelBooking(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private boolean isValidBookingRequest(BookingRequest request) {
        if (request.getStationId() == null || 
            request.getStartTime() == null || 
            request.getEndTime() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        
        // Check if start time is in the future
        if (request.getStartTime().isBefore(now)) {
            return false;
        }

        // Check if end time is after start time
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

    public static class BookingRequest {
        private Long stationId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public Long getStationId() {
            return stationId;
        }

        public void setStationId(Long stationId) {
            this.stationId = stationId;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }
    }
} 
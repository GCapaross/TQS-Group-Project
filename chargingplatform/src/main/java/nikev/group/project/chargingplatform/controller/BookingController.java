package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<ChargingSession> createBooking(@RequestBody ChargingSession booking) {
        if (booking.getStartTime() == null || booking.getEndTime() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bookingService.bookSlot(
            booking.getChargingStation().getId(),
            booking.getUser().getId(),
            booking.getStartTime(),
            booking.getEndTime()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargingSession> getBooking(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.getBooking(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
} 
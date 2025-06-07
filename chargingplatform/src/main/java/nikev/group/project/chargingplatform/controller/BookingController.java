package nikev.group.project.chargingplatform.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.controller.UserController;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.service.BookingService;
import nikev.group.project.chargingplatform.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(
        BookingController.class
    );

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer requestTimer;

    public BookingController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = Counter.builder("app_requests_total")
            .description("Total number of API requests")
            .tag("application", "chargingplatform")
            .register(meterRegistry);
        this.requestTimer = Timer.builder("app_requests_latency")
            .description("Request latency in seconds")
            .tag("application", "chargingplatform")
            .register(meterRegistry);
    }

    @PostMapping
    public ResponseEntity<Reservation> createBooking(
        @RequestBody(required = true) BookingRequestDTO request
    ) {
        requestCounter.increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            if (!isValidBookingRequest(request)) {
                sample.stop(
                    Timer.builder("app_requests_latency")
                        .tag("endpoint", "createBooking")
                        .tag("status", "failure")
                        .register(meterRegistry)
                );
                return ResponseEntity.badRequest().build();
            }
            Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
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

            sample.stop(
                Timer.builder("app_requests_latency")
                    .tag("endpoint", "createBooking")
                    .tag("status", "success")
                    .register(meterRegistry)
            );
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            sample.stop(
                Timer.builder("app_requests_latency")
                    .tag("endpoint", "createBooking")
                    .tag("status", "failure")
                    .register(meterRegistry)
            );
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
        @NotNull @PathVariable(required = true) Long id
    ) {
        requestCounter.increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            bookingService.cancelBooking(id);
            sample.stop(
                Timer.builder("app_requests_latency")
                    .tag("endpoint", "cancelBooking")
                    .tag("status", "success")
                    .register(meterRegistry)
            );
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            sample.stop(
                Timer.builder("app_requests_latency")
                    .tag("endpoint", "cancelBooking")
                    .tag("status", "failure")
                    .register(meterRegistry)
            );
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

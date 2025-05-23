package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargingSessionRepository;
import nikev.group.project.chargingplatform.repository.ChargingStationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BookingServiceTest {

    @Mock
    private ChargingSessionRepository chargingSessionRepository;

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private Station station;
    private User user;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        station = new Station();
        station.setId(1L);
        station.setStatus(Station.StationStatus.AVAILABLE);
        station.setAvailableSlots(2);
        station.setMaxSlots(2);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void whenBookingValidSlot_thenBookingIsCreated() {
        // Arrange
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chargingSessionRepository.findOverlappingSessions(any(), any(), any())).thenReturn(new ArrayList<>());
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Reservation booking = bookingService.bookSlot(1L, 1L, startTime, endTime);

        // Assert
        assertNotNull(booking);
        assertEquals("BOOKED", booking.getStatus());
        assertEquals(station, booking.getChargingStation());
        assertEquals(startTime, booking.getStartTime());
        assertEquals(endTime, booking.getEndTime());
        verify(chargingSessionRepository).save(any(Reservation.class));
    }

    @Test
    void whenBookingOverlappingSlot_thenThrowsException() {
        // Arrange
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        List<Reservation> overlappingSessions = new ArrayList<>();
        Reservation existingBooking = new Reservation();
        overlappingSessions.add(existingBooking);
        when(chargingSessionRepository.findOverlappingSessions(any(), any(), any())).thenReturn(overlappingSessions);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            bookingService.bookSlot(1L, 1L, startTime, endTime)
        );
    }

    @Test
    void whenStationNotAvailable_thenThrowsException() {
        // Arrange
        station.setStatus(Station.StationStatus.MAINTENANCE);
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            bookingService.bookSlot(1L, 1L, startTime, endTime)
        );
    }

    @Test
    void whenNoAvailableSlots_thenThrowsException() {
        // Arrange
        station.setAvailableSlots(0);
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            bookingService.bookSlot(1L, 1L, startTime, endTime)
        );
    }

    @Test
    void whenBookingLastSlot_thenStationStatusChangesToInUse() {
        // Arrange
        station.setAvailableSlots(1);
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chargingSessionRepository.findOverlappingSessions(any(), any(), any())).thenReturn(new ArrayList<>());
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(chargingStationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        bookingService.bookSlot(1L, 1L, startTime, endTime);

        // Assert
        assertEquals(Station.StationStatus.IN_USE, station.getStatus());
        assertEquals(0, station.getAvailableSlots());
    }

    @Test
    void whenCancellingBooking_thenSlotIsReleased() {
        // Arrange
        Reservation booking = new Reservation();
        booking.setId(1L);
        booking.setChargingStation(station);
        booking.setStatus("BOOKED");
        station.setAvailableSlots(1); // Set initial available slots to 1

        when(chargingSessionRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(chargingStationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        bookingService.cancelBooking(1L);

        // Assert
        assertEquals(Station.StationStatus.AVAILABLE, station.getStatus());
        assertEquals(2, station.getAvailableSlots()); // Should be 2 after cancellation
        verify(chargingSessionRepository).delete(booking);
    }
} 
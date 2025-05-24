package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import nikev.group.project.chargingplatform.repository.StationRepository;

@SpringBootTest
public class BookingServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StationRepository chargingStationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StationRepository stationRepository;

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
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station.setPricePerKwh(0.5);
        station.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
        station.setChargers(List.of(new Charger(111L, Charger.ChargerStatus.MAINTENANCE, 1.45)));
        station.setTimetable("24/7");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void whenBookingValidSlot_thenBookingIsCreated() {
        // Arrange
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Long stationId = 1L;
        when(reservationRepository.findOverlappingReservations(any(), any(), any())).thenReturn(new ArrayList<>());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Reservation reservation = bookingService.bookSlot(1L, 1L, startTime, endTime);

        // Assert
        assertNotNull(reservation);
        assertEquals(Reservation.ReservationStatus.BOOKED, reservation.getStatus());
        assertEquals(station, reservation.getStation());
        assertEquals(startTime, reservation.getStartDate());
        assertEquals(endTime, reservation.getEndDate());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void whenBookingOverlappingSlot_thenThrowsException() {
        // Arrange
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        List<Reservation> overlappingSessions = new ArrayList<>();
        Reservation existingBooking = new Reservation();
        overlappingSessions.add(existingBooking);
        when(reservationRepository.findOverlappingReservations(any(), any(), any())).thenReturn(overlappingSessions);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            bookingService.bookSlot(1L, 1L, startTime, endTime)
        );
    }

    @Test
    void whenStationNotAvailable_thenThrowsException() {
        // Arrange
        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            bookingService.bookSlot(1L, 1L, startTime, endTime)
        );
    }

    @Test
    void whenNoAvailableSlots_thenThrowsException() {
        // Arrange
        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> bookingService.bookSlot(1L, 1L, startTime, endTime)
        );
        assertNotNull(exception.getMessage());
    }

    @Test
    void whenBookingLastSlot_thenStationStatusChangesToInUse() {
        // Arrange
        Charger charger = new Charger();
        charger.setId(111L);
        charger.setStatus(Charger.ChargerStatus.AVAILABLE);
        charger.setChargingSpeedKw(1.45);
        station.setChargers(List.of(charger));

        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

         when(reservationRepository.findOverlappingReservations(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(stationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        bookingService.bookSlot(1L, 1L, startTime, endTime);

        // Assert
        assertThat(charger.getStatus()).isEqualTo(Charger.ChargerStatus.CHARGING);
        assertThat(station.hasAvailableCharger()).isFalse();
    }

    @Test
    void whenCancellingBooking_thenSlotIsReleased() {
        // Arrange
        Reservation booking = new Reservation();
        booking.setId(1L);
        station.setChargers(List.of(new Charger(111L, Charger.ChargerStatus.RESERVED, 1.45)));
        booking.setStation(station);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(chargingStationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        bookingService.cancelBooking(1L);

        // Assert
        assertThat(station.hasAvailableCharger()).isTrue();
        verify(reservationRepository).delete(booking);
    }
}
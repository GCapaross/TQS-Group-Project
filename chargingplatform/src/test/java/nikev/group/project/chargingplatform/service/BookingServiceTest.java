package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.repository.ChargingSessionRepository;
import nikev.group.project.chargingplatform.repository.ChargingStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ChargingSessionRepository chargingSessionRepository;

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @InjectMocks
    private BookingService bookingService;

    private ChargingStation station;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        station = new ChargingStation();
        station.setId(1L);
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        station.setAvailableSlots(2);
        station.setMaxSlots(2);

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void bookSlot_WhenStationAvailable_ShouldCreateBooking() {
        // Given
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(chargingSessionRepository.findOverlappingSessions(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When
        ChargingSession result = bookingService.bookSlot(1L, 1L, startTime, endTime);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("BOOKED");
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
        verify(chargingStationRepository).save(station);
        verify(chargingSessionRepository).save(any(ChargingSession.class));
    }

    @Test
    void bookSlot_WhenStationNotAvailable_ShouldThrowException() {
        // Given
        station.setStatus(ChargingStation.StationStatus.MAINTENANCE);
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));

        // When/Then
        assertThatThrownBy(() -> bookingService.bookSlot(1L, 1L, startTime, endTime))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Station is not available for booking");
    }

    @Test
    void bookSlot_WhenNoSlotsAvailable_ShouldThrowException() {
        // Given
        station.setAvailableSlots(0);
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));

        // When/Then
        assertThatThrownBy(() -> bookingService.bookSlot(1L, 1L, startTime, endTime))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No available slots at this station");
    }

    @Test
    void bookSlot_WhenTimeSlotBooked_ShouldThrowException() {
        // Given
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(chargingSessionRepository.findOverlappingSessions(any(), any(), any()))
                .thenReturn(Collections.singletonList(new ChargingSession()));

        // When/Then
        assertThatThrownBy(() -> bookingService.bookSlot(1L, 1L, startTime, endTime))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Time slot is already booked");
    }

    @Test
    void cancelBooking_ShouldUpdateStationAndDeleteSession() {
        // Given
        ChargingSession session = new ChargingSession();
        session.setId(1L);
        session.setChargingStation(station);
        when(chargingSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // When
        bookingService.cancelBooking(1L);

        // Then
        verify(chargingStationRepository).save(station);
        verify(chargingSessionRepository).delete(session);
    }
} 
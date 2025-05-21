package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private User testUser;
    private ChargingStation testStation;
    private ChargingSession testSession;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        // Setup test station
        testStation = new ChargingStation();
        testStation.setId(1L);
        testStation.setName("Test Station");
        testStation.setStatus(ChargingStation.StationStatus.AVAILABLE);
        testStation.setAvailableSlots(5);

        // Setup test session
        testSession = new ChargingSession();
        testSession.setId(1L);
        testSession.setUser(testUser);
        testSession.setChargingStation(testStation);
        testSession.setStatus("BOOKED");

        // Setup test times
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void createBooking_Success() {
        // Arrange
        when(bookingService.bookSlot(any(), any(), any(), any())).thenReturn(testSession);

        // Act
        ResponseEntity<ChargingSession> response = bookingController.createBooking(
            "Bearer test-token",
            createBookingRequest(testStation.getId(), startTime, endTime)
        );

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testSession.getId(), response.getBody().getId());
        assertEquals("BOOKED", response.getBody().getStatus());
        verify(bookingService, times(1)).bookSlot(any(), any(), any(), any());
    }

    @Test
    void createBooking_WhenServiceThrowsException_ReturnsBadRequest() {
        // Arrange
        when(bookingService.bookSlot(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Booking failed"));

        // Act
        ResponseEntity<ChargingSession> response = bookingController.createBooking(
            "Bearer test-token",
            createBookingRequest(testStation.getId(), startTime, endTime)
        );

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void createBooking_WithInvalidToken_ReturnsBadRequest() {
        // Act
        ResponseEntity<ChargingSession> response = bookingController.createBooking(
            "Invalid token",
            createBookingRequest(testStation.getId(), startTime, endTime)
        );

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void createBooking_WithNullRequest_ReturnsBadRequest() {
        // Act
        ResponseEntity<ChargingSession> response = bookingController.createBooking(
            "Bearer test-token",
            null
        );

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void createBooking_WithInvalidTimeRange_ReturnsBadRequest() {
        // Arrange
        LocalDateTime invalidEndTime = startTime.minusHours(1); // End time before start time

        // Act
        ResponseEntity<ChargingSession> response = bookingController.createBooking(
            "Bearer test-token",
            createBookingRequest(testStation.getId(), startTime, invalidEndTime)
        );

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void createBooking_WithPastStartTime_ReturnsBadRequest() {
        // Arrange
        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(1);

        // Act
        ResponseEntity<ChargingSession> response = bookingController.createBooking(
            "Bearer test-token",
            createBookingRequest(testStation.getId(), pastStartTime, endTime)
        );

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void cancelBooking_Success() {
        // Arrange
        doNothing().when(bookingService).cancelBooking(any());

        // Act
        ResponseEntity<Void> response = bookingController.cancelBooking(
            "Bearer test-token",
            testSession.getId()
        );

        // Assert
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(bookingService, times(1)).cancelBooking(testSession.getId());
    }

    @Test
    void cancelBooking_WhenServiceThrowsException_ReturnsBadRequest() {
        // Arrange
        doThrow(new RuntimeException("Cancellation failed"))
            .when(bookingService).cancelBooking(any());

        // Act
        ResponseEntity<Void> response = bookingController.cancelBooking(
            "Bearer test-token",
            testSession.getId()
        );

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void cancelBooking_WithInvalidToken_ReturnsBadRequest() {
        // Act
        ResponseEntity<Void> response = bookingController.cancelBooking(
            "Invalid token",
            testSession.getId()
        );

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void cancelBooking_WithNullId_ReturnsBadRequest() {
        // Act
        ResponseEntity<Void> response = bookingController.cancelBooking(
            "Bearer test-token",
            null
        );

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
    }

    private BookingController.BookingRequest createBookingRequest(
            Long stationId, LocalDateTime startTime, LocalDateTime endTime) {
        BookingController.BookingRequest request = new BookingController.BookingRequest();
        request.setStationId(stationId);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        return request;
    }
} 
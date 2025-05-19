package nikev.group.project.chargingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private ChargingSession testSession;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);

        testSession = new ChargingSession();
        testSession.setId(1L);
        testSession.setStatus("BOOKED");
        testSession.setStartTime(startTime);
        testSession.setEndTime(endTime);

        ChargingStation station = new ChargingStation();
        station.setId(1L);
        testSession.setChargingStation(station);

        User user = new User();
        user.setId(1L);
        testSession.setUser(user);
    }

    @Test
    void whenCreatingBooking_thenReturnsCreatedBooking() throws Exception {
        // Arrange
        when(bookingService.bookSlot(any(), any(), any(), any())).thenReturn(testSession);

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSession)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSession.getId()))
                .andExpect(jsonPath("$.status").value(testSession.getStatus()))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists());
    }

    @Test
    void whenCreatingBookingWithInvalidData_thenReturnsBadRequest() throws Exception {
        // Arrange
        testSession.setStartTime(null);

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSession)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenCancellingBooking_thenReturnsNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/bookings/{id}", testSession.getId()))
                .andExpect(status().isNoContent());

        verify(bookingService).cancelBooking(testSession.getId());
    }

    @Test
    void whenGettingBooking_thenReturnsBooking() throws Exception {
        // Arrange
        when(bookingService.getBooking(testSession.getId())).thenReturn(testSession);

        // Act & Assert
        mockMvc.perform(get("/api/bookings/{id}", testSession.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSession.getId()))
                .andExpect(jsonPath("$.status").value(testSession.getStatus()))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists());
    }

    @Test
    void whenGettingNonExistentBooking_thenReturnsNotFound() throws Exception {
        // Arrange
        when(bookingService.getBooking(any())).thenThrow(new RuntimeException("Booking not found"));

        // Act & Assert
        mockMvc.perform(get("/api/bookings/{id}", 999L))
                .andExpect(status().isNotFound());
    }
} 
package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.BookingService;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;

import org.flywaydb.core.internal.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import nikev.group.project.chargingplatform.model.ChargingStation;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters for tests
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private BookingService bookingService;
    
    @Autowired
    private WebApplicationContext context;

    private User testUser;
    private ChargingStation testStation;
    private ChargingSession testSession;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        // Setup MockMvc with security disabled
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testStation = new ChargingStation();
        testStation.setId(1L);
        testStation.setName("Test Station");
        testStation.setStatus(ChargingStation.StationStatus.AVAILABLE);
        testStation.setAvailableSlots(5);

        testSession = new ChargingSession();
        testSession.setId(1L);
        testSession.setUser(testUser);
        testSession.setChargingStation(testStation);
        testSession.setStatus("BOOKED");

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void createBooking_Success() throws Exception {
        // Arrange
        when(bookingService.bookSlot(any(), any(), any(), any())).thenReturn(testSession);

        // Act & Assert with MockMvc
        mockMvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(new BookingRequestDTO(testStation.getId(), startTime, endTime)))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("BOOKED"));
        
        // Verify service was called
        verify(bookingService, times(1)).bookSlot(any(), any(), any(), any());
    }

    @Test
    void createBooking_WhenServiceThrowsException_ReturnsBadRequest() throws Exception {
        when(bookingService.bookSlot(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Booking failed"));

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(new BookingRequestDTO(testStation.getId(), startTime, endTime))))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithNullRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void createBooking_WithInvalidTimeRange_ReturnsBadRequest() throws Exception {
        LocalDateTime invalidEndTime = startTime.minusHours(1);
        
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(new BookingRequestDTO(testStation.getId(), startTime, invalidEndTime))))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithPastStartTime_ReturnsBadRequest() throws Exception {
        // Arrange
        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(1);

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(new BookingRequestDTO(testStation.getId(), pastStartTime, endTime))))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void cancelBooking_Success() throws Exception {
        // Arrange
        doNothing().when(bookingService).cancelBooking(any());

        // Act & Assert
        mockMvc.perform(delete("/api/bookings/" + testSession.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());
        
        // Verify service was called
        verify(bookingService, times(1)).cancelBooking(testSession.getId());
    }

    @Test
    void cancelBooking_WhenServiceThrowsException_ReturnsBadRequest() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Cancellation failed"))
            .when(bookingService).cancelBooking(any());

        // Act & Assert
        mockMvc.perform(delete("/api/bookings/" + testSession.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void cancelBooking_WithNullId_ReturnsBadRequest() throws Exception {
        // Testing with a non-numeric value which should cause validation failure
        mockMvc.perform(delete("/api/bookings/null")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.BookingService;

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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;

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
    private Station testStation;
    private Reservation testReservation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testStation = new Station();
        testStation.setId(1L);
        testStation.setName("Test Station");
        testStation.setLocation("Test Location");
        testStation.setPricePerKwh(0.25);
        testStation.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
        testStation.setChargers(List.of(new Charger(1L, Charger.ChargerStatus.RESERVED, 1.24)));
        testStation.setTimetable("24/7");

        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setUser(testUser);
        testReservation.setStation(testStation);

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void createBooking_Success() throws Exception {
        when(bookingService.bookSlot(any(), any(), any(), any())).thenReturn(testReservation);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(new BookingRequestDTO(testStation.getId(), startTime, endTime))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.startDate").value(startTime));

        verify(bookingService, times(1)).bookSlot(any(), any(), any(), any());
    }

    @Test
    void createBooking_WhenServiceThrowsException_ReturnsBadRequest() throws Exception {
        when(bookingService.bookSlot(any(), any(), any(), any())).thenThrow(new RuntimeException("Booking failed"));

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
        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(1);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(new BookingRequestDTO(testStation.getId(), pastStartTime, endTime))))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void cancelBooking_Success() throws Exception {
        doNothing().when(bookingService).cancelBooking(any());

        mockMvc.perform(delete("/api/bookings/" + testReservation.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(bookingService, times(1)).cancelBooking(testReservation.getId());
    }

    @Test
    void cancelBooking_WhenServiceThrowsException_ReturnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Cancellation failed")).when(bookingService).cancelBooking(any());

        mockMvc.perform(delete("/api/bookings/" + testReservation.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void cancelBooking_WithNullId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/bookings/null")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
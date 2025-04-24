package nikev.group.project.chargingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.service.ChargingStationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChargingStationController.class)
class ChargingStationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChargingStationService chargingStationService;

    private ChargingStation station;

    @BeforeEach
    void setUp() {
        station = new ChargingStation();
        station.setId(1L);
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station.setMaxSlots(4);
        station.setAvailableSlots(2);
        station.setPricePerKwh(0.5);
    }

    @Test
    void getAllChargingStations_ShouldReturnAllStations() throws Exception {
        // Given
        when(chargingStationService.getAllChargingStations())
                .thenReturn(Arrays.asList(station));

        // When/Then
        mockMvc.perform(get("/api/charging-stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Station"))
                .andExpect(jsonPath("$[0].location").value("Test Location"));
    }

    @Test
    void getChargingStationById_WhenExists_ShouldReturnStation() throws Exception {
        // Given
        when(chargingStationService.getChargingStationById(1L))
                .thenReturn(station);

        // When/Then
        mockMvc.perform(get("/api/charging-stations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Station"))
                .andExpect(jsonPath("$.location").value("Test Location"));
    }

    @Test
    void createChargingStation_ShouldReturnCreatedStation() throws Exception {
        // Given
        when(chargingStationService.createChargingStation(any(ChargingStation.class)))
                .thenReturn(station);

        // When/Then
        mockMvc.perform(post("/api/charging-stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(station)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Station"))
                .andExpect(jsonPath("$.location").value("Test Location"));
    }

    @Test
    void updateChargingStation_ShouldReturnUpdatedStation() throws Exception {
        // Given
        when(chargingStationService.updateChargingStation(any(Long.class), any(ChargingStation.class)))
                .thenReturn(station);

        // When/Then
        mockMvc.perform(put("/api/charging-stations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(station)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Station"))
                .andExpect(jsonPath("$.location").value("Test Location"));
    }

    @Test
    void deleteChargingStation_ShouldReturnNoContent() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/charging-stations/1"))
                .andExpect(status().isNoContent());

        verify(chargingStationService).deleteChargingStation(1L);
    }
} 
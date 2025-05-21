package nikev.group.project.chargingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.service.ChargingStationService;
import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChargingStationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChargingStationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
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
        station.setConnectorTypes(Arrays.asList("CCS", "Type 2"));
        station.setChargingSpeedKw(50.0);
        station.setCarrierNetwork("Test Network");
        station.setAverageRating(4.5);
    }

    @Test
    void getAllChargingStations_ShouldReturnAllStations() throws Exception {
        when(chargingStationService.getAllChargingStations())
                .thenReturn(Arrays.asList(station));

        mockMvc.perform(get("/api/charging-stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Station"))
                .andExpect(jsonPath("$[0].location").value("Test Location"));
    }

    @Test
    void getChargingStationById_WhenExists_ShouldReturnStation() throws Exception {
        when(chargingStationService.getChargingStationById(1L))
                .thenReturn(station);

        mockMvc.perform(get("/api/charging-stations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Station"))
                .andExpect(jsonPath("$.location").value("Test Location"));
    }

    @Test
    void getNearbyStations_ShouldReturnStationsInRadius() throws Exception {
        when(chargingStationService.findNearbyStations(40.7128, -74.0060, 10.0))
                .thenReturn(Arrays.asList(station));

        mockMvc.perform(get("/api/charging-stations/nearby")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060")
                .param("radiusKm", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Station"))
                .andExpect(jsonPath("$[0].location").value("Test Location"));
    }

    @Test
    void searchStations_WithFilters_ShouldReturnFilteredStations() throws Exception {
        List<String> connectorTypes = Arrays.asList("CCS", "Type 2");
        SearchStationDTO searchStationDTO = new SearchStationDTO();
        searchStationDTO.setConnectorTypes(connectorTypes);
        searchStationDTO.setMinChargingSpeed(50.0);
        searchStationDTO.setCarrierNetwork("Test Network");
        searchStationDTO.setMinRating(4.0);
        searchStationDTO.setLatitude(40.7128);
        searchStationDTO.setLongitude(-74.0060);
        searchStationDTO.setRadiusKm(10.0);

        when(chargingStationService.searchStations(any(SearchStationDTO.class)))
                .thenReturn(Arrays.asList(station));
        mockMvc.perform(post("/api/charging-stations/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchStationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Station"))
                .andExpect(jsonPath("$[0].location").value("Test Location"));
    }
        

    @Test
    void createChargingStation_ShouldReturnCreatedStation() throws Exception {
        when(chargingStationService.createChargingStation(any(ChargingStation.class)))
                .thenReturn(station);

        mockMvc.perform(post("/api/charging-stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(station)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Station"))
                .andExpect(jsonPath("$.location").value("Test Location"));
    }

    @Test
    void updateChargingStation_ShouldReturnUpdatedStation() throws Exception {
        when(chargingStationService.updateChargingStation(any(Long.class), any(ChargingStation.class)))
                .thenReturn(station);

        mockMvc.perform(put("/api/charging-stations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(station)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Station"))
                .andExpect(jsonPath("$.location").value("Test Location"));
    }

    @Test
    void deleteChargingStation_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/charging-stations/1"))
                .andExpect(status().isNoContent());

        verify(chargingStationService).deleteChargingStation(1L);
    }
} 
package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.micrometer.core.instrument.MeterRegistry;
// import io.prometheus.metrics.core.metrics.Counter;
import io.micrometer.core.instrument.Counter; // ✅ Correct
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.core.util.Json;
import java.util.*;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.security.JwtTokenProvider;
import nikev.group.project.chargingplatform.service.StationService;
import org.flywaydb.core.internal.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StationController.class)
public class StationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private StationService stationService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private MeterRegistry meterRegistry;  


  @BeforeEach
void setup() {
    Counter mockCounter = mock(Counter.class);
    Timer mockTimer = mock(Timer.class);

    when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(mockCounter);
    when(meterRegistry.timer(anyString(), any(String[].class))).thenReturn(mockTimer);
}

  /* FUNCTION public ResponseEntity<List<Station>> getAllStations() */
  /**
   * Given no stations
   * When get all stations
   * Then response with status 200 and 0 Station is returned
   */
  @Test
  void whenGetAllStationsAndNoStations_thenReturnEmptyList() {
    when(stationService.getAllStations()).thenReturn(Collections.emptyList());
    try {
      mockMvc
        .perform(get("/api/charging-stations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Given 3 stations
   * When get all stations
   * Then response with status 200 and 3 Station is returned
   */
  @Test
  void whenGetAllStationsAndHasStations_thenReturnAllStations() {
    Station station1 = new Station();
    station1.setId(1L);
    station1.setName("Station 1");
    Station station2 = new Station();
    station2.setId(2L);
    station2.setName("Station 2");
    Station station3 = new Station();
    station3.setId(3L);
    station3.setName("Station 3");
    when(stationService.getAllStations()).thenReturn(
      List.of(station1, station2, station3)
    );
    try {
      mockMvc
        .perform(get("/api/charging-stations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].name", is("Station 1")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].name", is("Station 2")))
        .andExpect(jsonPath("$[2].id", is(3)))
        .andExpect(jsonPath("$[2].name", is("Station 3")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * FUNCTION public ResponseEntity<Station> getStationById(@PathVariable Long id)
   */
  /**
   * Given a station with id 1 exists
   * When get station by id 1
   * Then response with status 200 and the Station with id 1 is returned
   */
  @Test
  void whenGetStationByIdAndExists_thenReturnStation() {
    Station station = new Station();
    station.setId(1L);
    station.setName("Station 1");
    when(stationService.getStationById(anyLong())).thenReturn(station);

    try {
      mockMvc
        .perform(get("/api/charging-stations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("Station 1")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * GIven no station with id 1 exists
   * When get station by id 1
   * Then response with status 404 and an error message is returned
   */
  @Test
  void whenGetStationByIdAndNotExists_thenReturnNotFound() {
    when(stationService.getStationById(anyLong())).thenThrow(
      new RuntimeException("Station not found")
    );

    try {
      mockMvc
        .perform(get("/api/charging-stations/1"))
        .andExpect(status().isNotFound());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * FUNCTION public ResponseEntity<List<Station>> getNearbyStations(double
   * latitude, double longitude, double radiusKm)
   */
  /**
   * Given 5 stations and a lat and lon
   * One at a distance of 1km of lat and lon
   * Two at a distance of 3km of lat and lon
   * Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 0.5km
   * Then response with status 200 and 0 found stations are returned
   */
  @Test
  void whenGetNearbyStations_thenReturnNoStations() {
    when(
      stationService.findNearbyStations(anyDouble(), anyDouble(), anyDouble())
    ).thenReturn(Collections.emptyList());
    try {
      mockMvc
        .perform(
          get("/api/charging-stations/nearby?latitude=0&longitude=0&radius=0.5")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Given 5 stations and a lat and lon
   * One at a distance of 1km of lat and lon
   * Two at a distance of 3km of lat and lon
   * Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 3km
   * Then response with status 200 and 3 found stations are returned
   */
  @Test
  void whenGetNearbyStations_thenReturn3Stations() {
    Station station1 = new Station();
    station1.setId(1L);
    station1.setName("Station 1");
    Station station2 = new Station();
    station2.setId(2L);
    station2.setName("Station 2");
    Station station3 = new Station();
    station3.setId(3L);
    station3.setName("Station 3");
    when(
      stationService.findNearbyStations(anyDouble(), anyDouble(), anyDouble())
    ).thenReturn(List.of(station1, station2, station3));

    try {
      mockMvc
        .perform(
          get("/api/charging-stations/nearby?latitude=0&longitude=0&radius=3")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * FUNCTION public ResponseEntity<List<Station>> searchStations(SearchStationDTO
   * searchStationRequest)
   */
  /**
   * Given a search request with a name
   * When searching for stations with that name
   * Then response with status 200 and the list of matching stations is returned
   */
  @Test
  void whenSearchStationsByName_thenReturnMatchingStations() {
    Station station1 = new Station();
    station1.setId(1L);
    station1.setName("Test Station 1");
    Station station2 = new Station();
    station2.setId(2L);
    station2.setName("Test Station 2");

    when(stationService.searchStations(any(SearchStationDTO.class))).thenReturn(
      List.of(station1, station2)
    );

    SearchStationDTO searchRequest = new SearchStationDTO();
    searchRequest.setName("Test Station");

    try {
      mockMvc
        .perform(
          post("/api/charging-stations/search")
            .contentType("application/json")
            .content(JsonUtils.toJson(searchRequest))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Test Station 1")))
        .andExpect(jsonPath("$[1].name", is("Test Station 2")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* FUNCTION public ResponseEntity<Station> createStation(Station station) */
  /**
   * Given *
   * When creating a station
   * then response with status 200 and the created station is returned
   */
  @Test
  void whenCreateStation_thenReturnCreatedStation() {
    Station station = new Station();
    station.setId(1L);
    station.setName("New Station");

    when(stationService.createStation(any(Station.class))).thenReturn(station);

    Station stationRequest = new Station(
      null,
      "New Station",
      "Location",
      0.0,
      0.0,
      0.0,
      null,
      null,
      null,
      null
    );
    try {
      mockMvc
        .perform(
          post("/api/charging-stations")
            .contentType("application/json")
            .content(JsonUtils.toJson(stationRequest))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("New Station")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * FUNCTION public ResponseEntity<Station> updateStation(Long id, Station
   * station)
   */
  /**
   * Given a station with id 5
   * When updating data from staion with id 5
   * Then response with status 200 and the updated station is returned
   */
  @Test
  void whenUpdateStation_thenReturnUpdatedStation() {
    Station station = new Station();
    station.setId(5L);
    station.setName("Updated Station");

    when(
      stationService.updateStation(anyLong(), any(Station.class))
    ).thenReturn(station);

    Station stationRequest = new Station(
      5L,
      "Updated Station",
      "New Location",
      1.0,
      1.0,
      1.0,
      null,
      null,
      null,
      null
    );
    try {
      mockMvc
        .perform(
          put("/api/charging-stations/5")
            .contentType("application/json")
            .content(JsonUtils.toJson(stationRequest))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(5)))
        .andExpect(jsonPath("$.name", is("Updated Station")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* FUNCTION public ResponseEntity<Void> deleteStation(Long id) */
  /**
   * Given a station with id 5
   * When deleting staion with id 1
   * Then response with status 404 is returned
   */
  @Test
  void whenDeleteStationAndNotExists_thenReturnNotFound() {
    doThrow(new RuntimeException("Station not found"))
      .when(stationService)
      .deleteStation(anyLong());

    try {
      mockMvc
        .perform(delete("/api/charging-stations/5"))
        .andExpect(status().isNotFound());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Given a station with id 5
   * When deleting staion with id 5
   * Then response with status 200 (ok) is returned
   */
  @Test
  void whenDeleteStationAndExists_thenReturnOk() {
    doNothing().when(stationService).deleteStation(anyLong());

    try {
      mockMvc
        .perform(delete("/api/charging-stations/5"))
        .andExpect(status().is(204));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

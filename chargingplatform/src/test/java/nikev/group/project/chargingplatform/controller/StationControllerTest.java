package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@WebMvcTest(StationController.class)
public class StationControllerTest {
  /* FUNCTION public ResponseEntity<List<Station>> getAllStations() */
  /**
   * Given no stations
   * When get all stations
   * Then response with status 200 and 0 Station is returned
   */
  /**
   * Given 3 stations
   * When get all stations
   * Then response with status 200 and 3 Station is returned
   */

  /*
   * FUNCTION public ResponseEntity<Station> getStationById(@PathVariable Long id)
   */
  /**
   * Given a station with id 1 exists
   * When get station by id 1
   * Then response with status 200 and the Station with id 1 is returned
   */
  /**
   * GIven no station with id 1 exists
   * When get station by id 1
   * Then response with status 404 and an error message is returned
   */
  /**
   * Given a station with id 1 exists
   * When get station by id 5 that does not exist
   * Then response with status 404 and an error message is returned
   */

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
  /**
   * Given 5 stations and a lat and lon
   * One at a distance of 1km of lat and lon
   * Two at a distance of 3km of lat and lon
   * Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 3km
   * Then response with status 200 and 3 found stations are returned
   */
  /**
   * Given 5 stations and a lat and lon
   * One at a distance of 1km of lat and lon
   * Two at a distance of 3km of lat and lon
   * Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 10km
   * Then response with status 200 and 5 found stations are returned
   */

  /*
   * FUNCTION public ResponseEntity<List<Station>> searchStations(SearchStationDTO
   * searchStationRequest)
   */
  /**
   * Given a search request with a name
   * When searching for stations with that name
   * Then response with status 200 and the list of matching stations is returned
   */

  /* FUNCTION public ResponseEntity<Station> createStation(Station station) */
  /**
   * Given *
   * When creating a station
   * then response with status 200 and the created station is returned
   */

  /*
   * FUNCTION public ResponseEntity<Station> updateStation(Long id, Station
   * station)
   */
  /**
   * Given a station with id 5
   * When updating data from staion with id 5
   * Then response with status 200 and the updated station is returned
   */

  /* FUNCTION public ResponseEntity<Void> deleteStation(Long id) */
  /**
   * Given a station with id 5
   * When deleting staion with id 1
   * Then response with status 404 is returned
   */
  /**
   * Given a station with id 5
   * When deleting staion with id 5
   * Then response with status 200 (ok) is returned
   */
}

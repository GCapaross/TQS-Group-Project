package nikev.group.project.chargingplatform.controller;

import java.util.List;
import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charging-stations")
public class StationController {

  @Autowired
  private StationService stationService;

  /**
   * Given no stations
   * When get all stations
   * Then response with status 200 and 0 Station is returned
   */
  /**
   * Given 3 stations
   * When get all stations
   * Then response with status 200 and  3 Station is returned
   */
  @GetMapping
  public ResponseEntity<List<Station>> getAllStations() {
    return ResponseEntity.ok(stationService.getAllStations());
  }

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
  @GetMapping("/{id}")
  public ResponseEntity<Station> getStationById(@PathVariable Long id) {
    return ResponseEntity.ok(stationService.getStationById(id));
  }

  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 0.5km
   * Then response with status 200 and 0 found stations are returned
   */
  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 3km
   * Then response with status 200 and 3 found stations are returned
   */
  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 10km
   * Then response with status 200 and 5 found stations are returned
   */
  @GetMapping("/nearby")
  public ResponseEntity<List<Station>> getNearbyStations(
    @RequestParam double latitude,
    @RequestParam double longitude,
    @RequestParam(defaultValue = "10.0") double radiusKm
  ) {
    return ResponseEntity.ok(
      stationService.findNearbyStations(latitude, longitude, radiusKm)
    );
  }

  /**
   * Given a search request with a name
   * When searching for stations with that name
   * Then response with status 200 and the list of matching stations is returned
   */
  @PostMapping("/search")
  public ResponseEntity<List<Station>> searchStations(
    @RequestBody SearchStationDTO searchStationRequest
  ) {
    return ResponseEntity.ok(
      stationService.searchStations(searchStationRequest)
    );
  }

  /**
   * Given *
   * When creating a station
   * then response with status 200 and the created station is returned
   */
  @PostMapping
  public ResponseEntity<Station> createStation(@RequestBody Station station) {
    return ResponseEntity.ok(stationService.createStation(station));
  }

  /**
   * Given a station with id 5
   * When updating data from staion with id 5
   * Then response with status 200 and the updated station is returned
   */
  @PutMapping("/{id}")
  public ResponseEntity<Station> updateStation(
    @PathVariable Long id,
    @RequestBody Station station
  ) {
    return ResponseEntity.ok(stationService.updateStation(id, station));
  }

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
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
    stationService.deleteStation(id);
    return ResponseEntity.noContent().build();
  }
}

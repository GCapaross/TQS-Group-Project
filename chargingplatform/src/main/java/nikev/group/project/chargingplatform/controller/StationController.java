package nikev.group.project.chargingplatform.controller;

import java.util.List;

import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.DTOs.StationDTO;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.service.StationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import nikev.group.project.chargingplatform.DTOs.StationCreateDTO;
import nikev.group.project.chargingplatform.DTOs.StationResponseDTO;
import nikev.group.project.chargingplatform.DTOs.StationWithChargerSpeedsDTO;

@RestController
@RequestMapping("/api/charging-stations")
public class StationController {

  @Autowired
  private StationService stationService;

  @GetMapping
  public ResponseEntity<List<StationDTO>> getAllStations() {
    return ResponseEntity.ok(stationService.getAllStations());
  }

  @GetMapping("/{id}")
  public ResponseEntity<StationDTO> getStationById(@PathVariable Long id) {
    try {
      StationDTO station = stationService.getStationById(id);
      return ResponseEntity.ok(station);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

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

  @PostMapping("/search")
  public ResponseEntity<List<Station>> searchStations(
    @RequestBody SearchStationDTO searchStationRequest
  ) {
    return ResponseEntity.ok(
      stationService.searchStations(searchStationRequest)
    );
  }

  @PostMapping
  public ResponseEntity<StationResponseDTO> createStation(@RequestBody StationCreateDTO station) {
    return ResponseEntity.ok(stationService.createStation(station));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Station> updateStation(
    @PathVariable Long id,
    @RequestBody Station station
  ) {
    return ResponseEntity.ok(stationService.updateStation(id, station));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
    try {
      stationService.deleteStation(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }
}

package nikev.group.project.chargingplatform.controller;

import java.util.List;
import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

// @RestController
// @RequestMapping("/api/charging-stations")
// public class StationController {

//   @Autowired
//   private StationService stationService;

//   @Autowired
//   private MeterRegistry meterRegistry;

//   private final Counter stationCreationCounter;
//   private final Timer stationCreationTimer;

//   public StationController(MeterRegistry meterRegistry) {
//     this.meterRegistry = meterRegistry;
//     this.stationCreationCounter = Counter.builder("app_stations_created_total")
//         .description("Total number of stations created")
//         .tag("application", "chargingplatform")
//         .register(meterRegistry);
//     this.stationCreationTimer = Timer.builder("app_stations_creation_latency")
//         .description("Station creation latency in seconds")
//         .tag("application", "chargingplatform")
//         .register(meterRegistry);
//   }
@RestController
@RequestMapping("/api/charging-stations")
public class StationController {

  private final StationService stationService;
  private final MeterRegistry meterRegistry;
  private final Counter stationCreationCounter;
  private final Timer stationCreationTimer;

  public StationController(StationService stationService, MeterRegistry meterRegistry) {
    this.stationService = stationService;
    this.meterRegistry = meterRegistry;
    this.stationCreationCounter = Counter.builder("app_stations_created_total")
        .description("Total number of stations created")
        .tag("application", "chargingplatform")
        .register(meterRegistry);
    this.stationCreationTimer = Timer.builder("app_stations_creation_latency")
        .description("Station creation latency in seconds")
        .tag("application", "chargingplatform")
        .register(meterRegistry);
  }

  @GetMapping
  public ResponseEntity<List<Station>> getAllStations() {
    return ResponseEntity.ok(stationService.getAllStations());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Station> getStationById(@PathVariable Long id) {
    try {
      Station station = stationService.getStationById(id);
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
  public ResponseEntity<Station> createStation(@RequestBody Station station) {
    stationCreationCounter.increment();
    Timer.Sample sample = Timer.start(meterRegistry);
    
    try {
      Station createdStation = stationService.createStation(station);
      sample.stop(Timer.builder("app_stations_creation_latency")
          .tag("status", "success")
          .register(meterRegistry));
      return ResponseEntity.ok(createdStation);
    } catch (RuntimeException e) {
      sample.stop(Timer.builder("app_stations_creation_latency")
          .tag("status", "failure")
          .register(meterRegistry));
      return ResponseEntity.badRequest().build();
    }
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

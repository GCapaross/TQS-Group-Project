package nikev.group.project.chargingplatform.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.DTOs.StationCreateDTO;
import nikev.group.project.chargingplatform.DTOs.StationDTO;
import nikev.group.project.chargingplatform.DTOs.StationResponseDTO;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/charging-stations")
public class StationController {

    private final StationService stationService;
    private final MeterRegistry meterRegistry;
    private final Counter stationCreationCounter;

    public StationController(
        StationService stationService,
        MeterRegistry meterRegistry
    ) {
        this.stationService = stationService;
        this.meterRegistry = meterRegistry;
        this.stationCreationCounter = Counter.builder(
            "app_stations_created_total"
        )
            .description("Total number of stations created")
            .tag("application", "chargingplatform")
            .register(meterRegistry);
    }

    @GetMapping
    public ResponseEntity<List<StationDTO>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @PostMapping
    public ResponseEntity<StationResponseDTO> createStation(
        @RequestBody StationCreateDTO station
    ) {
        stationCreationCounter.increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            sample.stop(
                Timer.builder("app_stations_creation_latency")
                    .tag("status", "success")
                    .register(meterRegistry)
            );
            return ResponseEntity.ok(stationService.createStation(station));
        } catch (RuntimeException e) {
            sample.stop(
                Timer.builder("app_stations_creation_latency")
                    .tag("status", "failure")
                    .register(meterRegistry)
            );
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationDTO> getStationById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            StationDTO station = stationService.getStationById(id);
            return ResponseEntity.ok(station);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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
}

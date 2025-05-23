package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.service.ChargingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/charging-stations")
public class ChargingStationController {

    @Autowired
    private ChargingStationService chargingStationService;

    @GetMapping
    public ResponseEntity<List<Station>> getAllChargingStations() {
        return ResponseEntity.ok(chargingStationService.getAllChargingStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getChargingStationById( 
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(chargingStationService.getChargingStationById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Station>> getNearbyStations(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10.0") double radiusKm) {
        return ResponseEntity.ok(chargingStationService.findNearbyStations(latitude, longitude, radiusKm));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Station>> searchStations(
            @RequestBody SearchStationDTO searchStationRequest) {
        return ResponseEntity.ok(chargingStationService.searchStations(searchStationRequest));
    }

    @PostMapping
    public ResponseEntity<Station> createChargingStation(@RequestBody Station chargingStation) {
        return ResponseEntity.ok(chargingStationService.createChargingStation(chargingStation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> updateChargingStation(
            @PathVariable Long id,
            @RequestBody Station chargingStation) {
        return ResponseEntity.ok(chargingStationService.updateChargingStation(id, chargingStation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChargingStation(@PathVariable Long id) {
        chargingStationService.deleteChargingStation(id);
        return ResponseEntity.noContent().build();
    }
} 
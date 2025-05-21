package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.ChargingStation;
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
    public ResponseEntity<List<ChargingStation>> getAllChargingStations() {
        return ResponseEntity.ok(chargingStationService.getAllChargingStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargingStation> getChargingStationById( 
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(chargingStationService.getChargingStationById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ChargingStation>> getNearbyStations(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10.0") double radiusKm) {
        return ResponseEntity.ok(chargingStationService.findNearbyStations(latitude, longitude, radiusKm));
    }

@GetMapping("/search")
public ResponseEntity<List<ChargingStation>> searchStations(
            @RequestBody SearchStationDTO searchStationRequest) {
        return ResponseEntity.ok(chargingStationService.searchStations(searchStationRequest));
    }

    @PostMapping
    public ResponseEntity<ChargingStation> createChargingStation(@RequestBody ChargingStation chargingStation) {
        return ResponseEntity.ok(chargingStationService.createChargingStation(chargingStation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChargingStation> updateChargingStation(
            @PathVariable Long id,
            @RequestBody ChargingStation chargingStation) {
        return ResponseEntity.ok(chargingStationService.updateChargingStation(id, chargingStation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChargingStation(@PathVariable Long id) {
        chargingStationService.deleteChargingStation(id);
        return ResponseEntity.noContent().build();
    }
} 
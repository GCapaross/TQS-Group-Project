package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.service.ChargingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ChargingStation> getChargingStationById(@PathVariable Long id) {
        return ResponseEntity.ok(chargingStationService.getChargingStationById(id));
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
package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.repository.ChargingStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChargingStationService {

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    public List<ChargingStation> getAllChargingStations() {
        return chargingStationRepository.findAll();
    }

    public ChargingStation getChargingStationById(Long id) {
        return chargingStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charging station not found with id: " + id));
    }

    public ChargingStation createChargingStation(ChargingStation chargingStation) {
        return chargingStationRepository.save(chargingStation);
    }

    public ChargingStation updateChargingStation(Long id, ChargingStation chargingStationDetails) {
        ChargingStation chargingStation = getChargingStationById(id);
        chargingStation.setName(chargingStationDetails.getName());
        chargingStation.setLocation(chargingStationDetails.getLocation());
        chargingStation.setStatus(chargingStationDetails.getStatus());
        chargingStation.setLatitude(chargingStationDetails.getLatitude());
        chargingStation.setLongitude(chargingStationDetails.getLongitude());
        return chargingStationRepository.save(chargingStation);
    }

    public void deleteChargingStation(Long id) {
        chargingStationRepository.deleteById(id);
    }
} 
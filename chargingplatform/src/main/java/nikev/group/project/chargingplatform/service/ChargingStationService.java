package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.repository.ChargingStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
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

    public List<ChargingStation> findNearbyStations(double latitude, double longitude, double radiusKm) {
        // Using Haversine formula to calculate distance
        return chargingStationRepository.findAll((root, query, cb) -> {
            // Convert radius from km to degrees (approximate)
            double radiusDegrees = radiusKm / 111.0;
            
            Predicate latPredicate = cb.between(root.get("latitude"), 
                latitude - radiusDegrees, latitude + radiusDegrees);
            Predicate longPredicate = cb.between(root.get("longitude"), 
                longitude - radiusDegrees, longitude + radiusDegrees);
            
            return cb.and(latPredicate, longPredicate);
        });
    }

    public List<ChargingStation> searchStations(
            List<String> connectorTypes,
            Double minChargingSpeed,
            String carrierNetwork,
            Double minRating,
            Double latitude,
            Double longitude,
            Double radiusKm) {
        
        return chargingStationRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (connectorTypes != null && !connectorTypes.isEmpty()) {
                predicates.add(root.get("connectorTypes").in(connectorTypes));
            }
            
            if (minChargingSpeed != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("chargingSpeedKw"), minChargingSpeed));
            }
            
            if (carrierNetwork != null) {
                predicates.add(cb.equal(root.get("carrierNetwork"), carrierNetwork));
            }
            
            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }
            
            if (latitude != null && longitude != null && radiusKm != null) {
                double radiusDegrees = radiusKm / 111.0;
                predicates.add(cb.between(root.get("latitude"), 
                    latitude - radiusDegrees, latitude + radiusDegrees));
                predicates.add(cb.between(root.get("longitude"), 
                    longitude - radiusDegrees, longitude + radiusDegrees));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        });
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
        chargingStation.setConnectorTypes(chargingStationDetails.getConnectorTypes());
        chargingStation.setChargingSpeedKw(chargingStationDetails.getChargingSpeedKw());
        chargingStation.setCarrierNetwork(chargingStationDetails.getCarrierNetwork());
        return chargingStationRepository.save(chargingStation);
    }

    public void deleteChargingStation(Long id) {
        chargingStationRepository.deleteById(id);
    }
} 
package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Station;
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

    public List<Station> getAllChargingStations() {
        return chargingStationRepository.findAll();
    }

    public Station getChargingStationById(Long id) {
        return chargingStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charging station not found with id: " + id));
    }

    public List<Station> findNearbyStations(double latitude, double longitude, double radiusKm) {
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

    public List<Station> searchStations(
            SearchStationDTO searchStation) {
        
        return chargingStationRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (searchStation.getConnectorTypes() != null && !searchStation.getConnectorTypes().isEmpty()) {
                predicates.add(root.get("connectorTypes").in(searchStation.getConnectorTypes()));
            }
            
            if (searchStation.getMinChargingSpeed() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("chargingSpeedKw"), searchStation.getMinChargingSpeed()));
            }
            
            if (searchStation.getCarrierNetwork() != null) {
                predicates.add(cb.equal(root.get("carrierNetwork"), searchStation.getCarrierNetwork()));
            }
            
            if (searchStation.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), searchStation.getMinRating()));
            }
            
            if (searchStation.getLatitude() != null && searchStation.getLongitude() != null && searchStation.getRadiusKm() != null) {
                double radiusDegrees = searchStation.getRadiusKm() / 111.0;
                predicates.add(cb.between(root.get("latitude"), 
                    searchStation.getLatitude() - radiusDegrees, searchStation.getLatitude() + radiusDegrees));
                predicates.add(cb.between(root.get("longitude"), 
                    searchStation.getLongitude() - radiusDegrees, searchStation.getLongitude() + radiusDegrees));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    public Station createChargingStation(Station chargingStation) {
        return chargingStationRepository.save(chargingStation);
    }

    public Station updateChargingStation(Long id, Station chargingStationDetails) {
        Station chargingStation = getChargingStationById(id);
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
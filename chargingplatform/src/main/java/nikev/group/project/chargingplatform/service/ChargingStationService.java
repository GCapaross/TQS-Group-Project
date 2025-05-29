package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.repository.ChargingStationRepository;
import nikev.group.project.chargingplatform.repository.ChargingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChargingStationService {

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private ChargingSessionRepository chargingSessionRepository;

    public List<ChargingStation> getAllChargingStations() {
        List<ChargingStation> stations = chargingStationRepository.findAll();
        System.out.println("Found " + stations.size() + " charging stations");
        stations.forEach(station -> System.out.println("Station: " + station.getName() + ", Available Slots: " + station.getAvailableSlots()));
        return stations;
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

    @Transactional
    public ChargingSession startChargingSession(Long stationId, Long reservationId) {
        ChargingStation station = getChargingStationById(stationId);
        
        if (station.getStatus() != ChargingStation.StationStatus.AVAILABLE) {
            throw new RuntimeException("Station is not available for charging");
        }
        
        if (station.getAvailableSlots() <= 0) {
            throw new RuntimeException("No available slots at this station");
        }

        // If there's a reservation, verify it
        if (reservationId != null) {
            ChargingSession reservation = chargingSessionRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));
            
            if (!reservation.getStatus().equals("BOOKED")) {
                throw new RuntimeException("Invalid reservation status");
            }
            
            if (!reservation.getChargingStation().getId().equals(stationId)) {
                throw new RuntimeException("Reservation is for a different station");
            }
        }

        // Create new charging session
        ChargingSession session = new ChargingSession();
        session.setChargingStation(station);
        session.setStartTime(LocalDateTime.now());
        session.setStatus("IN_PROGRESS");
        session.setEnergyConsumed(0.0);
        session.setCost(0.0);

        // Update station status
        station.setAvailableSlots(station.getAvailableSlots() - 1);
        if (station.getAvailableSlots() == 0) {
            station.setStatus(ChargingStation.StationStatus.IN_USE);
        }
        
        chargingStationRepository.save(station);
        return chargingSessionRepository.save(session);
    }

    @Transactional
    public ChargingSession stopChargingSession(Long stationId) {
        ChargingStation station = getChargingStationById(stationId);
        
        // Find active session for this station
        ChargingSession session = chargingSessionRepository.findByChargingStationId(stationId)
                .stream()
                .filter(s -> s.getStatus().equals("IN_PROGRESS"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active charging session found"));

        // Update session
        session.setEndTime(LocalDateTime.now());
        session.setStatus("COMPLETED");
        
        // Calculate final cost
        double durationHours = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toMinutes() / 60.0;
        double energyConsumed = durationHours * station.getChargingSpeedKw();
        double cost = energyConsumed * station.getPricePerKwh();
        
        session.setEnergyConsumed(energyConsumed);
        session.setCost(cost);

        // Update station status
        station.setAvailableSlots(station.getAvailableSlots() + 1);
        if (station.getStatus() == ChargingStation.StationStatus.IN_USE) {
            station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        }
        
        chargingStationRepository.save(station);
        return chargingSessionRepository.save(session);
    }

    public Map<String, Object> getActiveSessionData(Long stationId) {
        ChargingSession session = chargingSessionRepository.findByChargingStationId(stationId)
                .stream()
                .filter(s -> s.getStatus().equals("IN_PROGRESS"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active charging session found"));

        return Map.of(
            "energySupplied", session.getEnergyConsumed(),
            "cost", session.getCost(),
            "startTime", session.getStartTime().toString(),
            "isActive", true
        );
    }
} 
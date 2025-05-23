package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.repository.ChargingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ChargingSessionRepository chargingSessionRepository;

    public Map<String, Object> getUserConsumptionStats(Long userId) {
        List<Reservation> sessions = chargingSessionRepository.findByUserId(userId);
        
        double totalEnergyConsumed = sessions.stream()
                .mapToDouble(Reservation::getEnergyConsumed)
                .sum();
        
        double totalCost = sessions.stream()
                .mapToDouble(Reservation::getCost)
                .sum();
        
        long totalSessions = sessions.size();
        
        return Map.of(
            "totalEnergyConsumed", totalEnergyConsumed,
            "totalCost", totalCost,
            "totalSessions", totalSessions,
            "sessions", sessions
        );
    }

    public Map<String, Object> getStationStats(Long stationId) {
        List<Reservation> sessions = chargingSessionRepository.findByChargingStationId(stationId);
        
        double totalEnergyDelivered = sessions.stream()
                .mapToDouble(Reservation::getEnergyConsumed)
                .sum();
        
        double totalRevenue = sessions.stream()
                .mapToDouble(Reservation::getCost)
                .sum();
        
        long totalSessions = sessions.size();
        
        return Map.of(
            "totalEnergyDelivered", totalEnergyDelivered,
            "totalRevenue", totalRevenue,
            "totalSessions", totalSessions,
            "sessions", sessions
        );
    }

    public Map<String, Object> getAggregateStats() {
        List<Reservation> allSessions = chargingSessionRepository.findAll();
        
        double totalEnergy = allSessions.stream()
                .mapToDouble(Reservation::getEnergyConsumed)
                .sum();
        
        double totalRevenue = allSessions.stream()
                .mapToDouble(Reservation::getCost)
                .sum();
        
        Map<String, Long> sessionsByStatus = allSessions.stream()
                .collect(Collectors.groupingBy(
                    Reservation::getStatus,
                    Collectors.counting()
                ));
        
        return Map.of(
            "totalEnergy", totalEnergy,
            "totalRevenue", totalRevenue,
            "sessionsByStatus", sessionsByStatus
        );
    }
} 
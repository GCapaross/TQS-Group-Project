package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.ChargingSession;
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
        List<ChargingSession> sessions = chargingSessionRepository.findByUserId(userId);
        
        double totalEnergyConsumed = sessions.stream()
                .mapToDouble(ChargingSession::getEnergyConsumed)
                .sum();
        
        double totalCost = sessions.stream()
                .mapToDouble(ChargingSession::getCost)
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
        List<ChargingSession> sessions = chargingSessionRepository.findByChargingStationId(stationId);
        
        double totalEnergyDelivered = sessions.stream()
                .mapToDouble(ChargingSession::getEnergyConsumed)
                .sum();
        
        double totalRevenue = sessions.stream()
                .mapToDouble(ChargingSession::getCost)
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
        List<ChargingSession> allSessions = chargingSessionRepository.findAll();
        
        double totalEnergy = allSessions.stream()
                .mapToDouble(ChargingSession::getEnergyConsumed)
                .sum();
        
        double totalRevenue = allSessions.stream()
                .mapToDouble(ChargingSession::getCost)
                .sum();
        
        Map<String, Long> sessionsByStatus = allSessions.stream()
                .collect(Collectors.groupingBy(
                    ChargingSession::getStatus,
                    Collectors.counting()
                ));
        
        return Map.of(
            "totalEnergy", totalEnergy,
            "totalRevenue", totalRevenue,
            "sessionsByStatus", sessionsByStatus
        );
    }
} 
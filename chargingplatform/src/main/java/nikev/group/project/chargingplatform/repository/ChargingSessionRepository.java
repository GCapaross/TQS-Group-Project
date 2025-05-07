package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {
    List<ChargingSession> findByUserId(Long userId);
    List<ChargingSession> findByChargingStationId(Long stationId);
    
    @Query("SELECT cs FROM ChargingSession cs WHERE cs.chargingStation.id = :stationId " +
           "AND ((cs.startTime BETWEEN :startTime AND :endTime) " +
           "OR (cs.endTime BETWEEN :startTime AND :endTime))")
    List<ChargingSession> findOverlappingSessions(
        @Param("stationId") Long stationId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
} 
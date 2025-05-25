package nikev.group.project.chargingplatform.repository;

import java.time.LocalDateTime;
import java.util.List;
import nikev.group.project.chargingplatform.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository
  extends JpaRepository<Reservation, Long> {
  List<Reservation> findByUser_Id(Long userId);
  List<Reservation> findByStation_Id(Long stationId);

  // May be bad, as explained in discord
  @Query(
    "SELECT r FROM Reservation r WHERE r.station.id = :stationId " +
    "AND ((r.startDate BETWEEN :startDate AND :endDate) " +
    "OR (r.endDate BETWEEN :startDate AND :endDate) " +
    "OR (r.startDate <= :startDate AND r.endDate >= :endDate))"
  )
  List<Reservation> findOverlappingReservations(
    @Param("stationId") Long stationId,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
  );
}

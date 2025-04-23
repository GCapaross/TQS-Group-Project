package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
} 
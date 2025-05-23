package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingStationRepository extends JpaRepository<Station, Long>, JpaSpecificationExecutor<Station> {
} 
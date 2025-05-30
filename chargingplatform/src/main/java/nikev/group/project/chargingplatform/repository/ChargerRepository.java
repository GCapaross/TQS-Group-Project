package nikev.group.project.chargingplatform.repository;

import java.util.List;
import nikev.group.project.chargingplatform.model.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {
  List<Charger> findByStation_Id(Long stationId);
}

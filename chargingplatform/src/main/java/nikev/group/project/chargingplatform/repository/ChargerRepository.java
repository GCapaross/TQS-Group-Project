package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {
}

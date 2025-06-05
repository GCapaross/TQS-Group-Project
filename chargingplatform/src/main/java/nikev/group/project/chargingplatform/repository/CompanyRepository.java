package nikev.group.project.chargingplatform.repository;

import java.util.Optional;
import nikev.group.project.chargingplatform.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
}

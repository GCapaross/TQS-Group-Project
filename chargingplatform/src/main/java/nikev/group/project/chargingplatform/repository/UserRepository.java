package nikev.group.project.chargingplatform.repository;

import java.util.Optional;
import nikev.group.project.chargingplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndIdNot(String email, Long id);
    Optional<User> findByUsername(String username);
}

package nikev.group.project.chargingplatform.config;

import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        // Create default admin user if it doesn't exist
        if (!userRepository.findByEmail("admin@admin.com").isPresent()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@admin.com");
            admin.setPassword("admin");
            userRepository.save(admin);
        }
    }
} 

// mvn spring-boot:run
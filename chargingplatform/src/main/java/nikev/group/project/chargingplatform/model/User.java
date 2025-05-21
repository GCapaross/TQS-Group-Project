package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    private String password;

    // Add role
    
    @OneToMany(mappedBy = "user")
    private List<ChargingSession> chargingSessions;
} 
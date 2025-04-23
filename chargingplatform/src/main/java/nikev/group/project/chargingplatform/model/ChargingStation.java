package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "charging_stations")
public class ChargingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String location;
    private String status; // AVAILABLE, IN_USE, MAINTENANCE
    private double latitude;
    private double longitude;
    
    @OneToMany(mappedBy = "chargingStation")
    private List<ChargingSession> chargingSessions;
} 
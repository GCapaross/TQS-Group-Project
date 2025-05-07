package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "charging_sessions")
public class ChargingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "charging_station_id")
    private ChargingStation chargingStation;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double energyConsumed;
    private String status; // BOOKED, IN_PROGRESS, COMPLETED, CANCELLED
    private double cost;
} 
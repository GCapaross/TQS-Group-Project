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
    
    @Enumerated(EnumType.STRING)
    private StationStatus status;
    
    private double latitude;
    private double longitude;
    
    @Column(name = "max_slots")
    private int maxSlots;
    
    @Column(name = "available_slots")
    private int availableSlots;
    
    @Column(name = "price_per_kwh")
    private double pricePerKwh;
    
    @OneToMany(mappedBy = "chargingStation")
    private List<ChargingSession> chargingSessions;
    
    public enum StationStatus {
        AVAILABLE,
        IN_USE,
        MAINTENANCE,
        OUT_OF_SERVICE
    }
} 
package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "stations")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String location;
    
    
    private double latitude;
    private double longitude;
    
    @Column(name = "price_per_kwh")
    private double pricePerKwh;
    
    @ElementCollection
    @CollectionTable(name = "station_connector_types", joinColumns = @JoinColumn(name = "station_id"))
    @Column(name = "connector_type")
    private List<String> connectorTypes;
    
    @Column(name = "charging_speed_kw")
    private double chargingSpeedKw;
    
    @OneToMany(mappedBy = "chargingStation")
    @JsonIgnore
    private List<ChargingSession> chargingSessions;
    
    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL)
    private List<StationReview> reviews;
    
    public enum StationStatus {
        AVAILABLE,
        IN_USE,
        MAINTENANCE,
        OUT_OF_SERVICE
    }
} 
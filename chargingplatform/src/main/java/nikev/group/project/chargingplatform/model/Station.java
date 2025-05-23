package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @CollectionTable(name = "station_supported_connectors", joinColumns = @JoinColumn(name = "station_id"))
    @Column(name = "connector_type")
    private List<String> supportedConnectors; 
    
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Charger> chargers;

    private String timetable;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> workers;
    
    @OneToMany(mappedBy = "chargingStation")
    @JsonIgnore
    private List<Reservation> chargingSessions;

    public boolean hasAvailableCharger() {
        return this.chargers != null
            && this.chargers.stream()
                       .anyMatch(c -> c.getStatus() == Charger.ChargerStatus.AVAILABLE);
    }
}
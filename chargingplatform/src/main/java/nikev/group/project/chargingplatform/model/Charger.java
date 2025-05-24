package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chargers")
public class Charger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChargerStatus status;

    @Column(name = "charging_speed_kw")
    private double chargingSpeedKw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    public static enum ChargerStatus {
        AVAILABLE,      // Charger is free and operational
        CHARGING,       // Charger is currently in use
        RESERVED,       // Charger is reserved for a user
        OUT_OF_SERVICE, // Charger is not operational (e.g., broken)
        MAINTENANCE     // Charger is under maintenance
    }
}

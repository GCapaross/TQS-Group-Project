package nikev.group.project.chargingplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @JsonIgnore
    private Station station;

    public static enum ChargerStatus {
        AVAILABLE, // Charger is free and operational
        IN_USE, // Charger is currently in use
        OUT_OF_SERVICE, // Charger is not operational (e.g., broken)
    }
}

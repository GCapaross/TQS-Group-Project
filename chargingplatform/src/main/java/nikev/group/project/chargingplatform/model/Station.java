package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    
    private String timetable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
        name = "station_workers",
        joinColumns = @JoinColumn(name = "station_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> workers;
}
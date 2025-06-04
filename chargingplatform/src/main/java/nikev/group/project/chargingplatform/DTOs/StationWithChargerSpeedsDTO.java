package nikev.group.project.chargingplatform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import nikev.group.project.chargingplatform.model.Station;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationWithChargerSpeedsDTO {
    private Long id;
    private String name;
    private String location;
    private double latitude;
    private double longitude;
    private double pricePerKwh;
    private List<String> supportedConnectors;
    private String timetable;

    private List<Double> chargerSpeeds;

    /**
     * Constructor to create a StationWithChargerSpeedsDTO from a Station.
     *
     * @param station the Station to convert
     */
    public StationWithChargerSpeedsDTO(Station station) {
        this.id = station.getId();
        this.name = station.getName();
        this.location = station.getLocation();
        this.latitude = station.getLatitude();
        this.longitude = station.getLongitude();
        this.pricePerKwh = station.getPricePerKwh();
        this.supportedConnectors = station.getSupportedConnectors();
        this.timetable = station.getTimetable();
        
        this.chargerSpeeds = null;
    }
}

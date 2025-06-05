package nikev.group.project.chargingplatform.DTOs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}

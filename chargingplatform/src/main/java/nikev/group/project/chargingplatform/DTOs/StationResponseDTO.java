package nikev.group.project.chargingplatform.DTOs;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;

@Getter
@Setter
@NoArgsConstructor
public class StationResponseDTO {

    private Long id;
    private String name;
    private String location;
    private Double latitude;
    private Double longitude;
    private Double pricePerKwh;
    private List<String> supportedConnectors;
    private String timetable;
    private String companyName;
    private List<Long> workerIds;

    /**
     * returns a DTO representation of a Station entity.
     */
    public StationResponseDTO(Station station) {
        this.id = station.getId();
        this.name = station.getName();
        this.location = station.getLocation();
        this.latitude = station.getLatitude();
        this.longitude = station.getLongitude();
        this.pricePerKwh = station.getPricePerKwh();
        this.supportedConnectors = station.getSupportedConnectors();
        this.timetable = station.getTimetable();

        if (station.getCompany() != null) {
            this.companyName = station.getCompany().getName();
        }

        if (station.getWorkers() != null && !station.getWorkers().isEmpty()) {
            this.workerIds = station.getWorkers()
                                    .stream()
                                    .map(User::getId)
                                    .toList();
        }
    }
}

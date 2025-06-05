package nikev.group.project.chargingplatform.DTOs;

import java.util.List;

import nikev.group.project.chargingplatform.utils.Utils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;

@Getter
@Setter
@NoArgsConstructor
public class StationResponseDTO implements  StationBasicDTO {

    private Long id;
    private String name;
    private String location;
    private Double latitude;
    private Double longitude;
    private Double pricePerKwh;
    private List<String> supportedConnectors;
    private String companyName;
    private List<Long> workerIds;

    /**
     * returns a DTO representation of a Station entity.
     */
    public StationResponseDTO(Station station) {
        this.id = station.getId();
        Utils.updateStationsDTOs(station, this);
    }
}

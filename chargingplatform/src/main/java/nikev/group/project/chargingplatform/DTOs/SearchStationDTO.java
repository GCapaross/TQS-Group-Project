package nikev.group.project.chargingplatform.DTOs;



import java.util.List;

import lombok.*;

@Getter
@Setter
public class SearchStationDTO {
    private String name;
    private String location;
    private List<String> supportedConnectors;
    private Double minPricePerKwh;
    private Double maxPricePerKwh;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
}

package nikev.group.project.chargingplatform.DTOs;



import java.util.List;

import lombok.*;

@Getter
@Setter
public class SearchStationDTO {
    private List<String> connectorTypes;
    private Double minChargingSpeed;
    private String carrierNetwork;
    private Double minRating;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
}

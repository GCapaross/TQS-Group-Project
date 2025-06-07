package nikev.group.project.chargingplatform.DTOs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nikev.group.project.chargingplatform.model.Charger;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StationDTO {

    private Long id;
    private String name;
    private String location;
    private double latitude;
    private double longitude;
    private double pricePerKwh;
    private List<String> supportedConnectors;
    private String companyName;
    private List<WorkerDTO> workers;
    private String status;
    private List<Charger> chargers;
}

package nikev.group.project.chargingplatform.DTOs;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;

/**
 * DTO for creating a new charging station.
 * This class is used to transfer data when creating a new station.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StationCreateDTO {

    @NotBlank(message = "O nome da estação é obrigatório")
    private String name;

    private String location;

    @NotNull(message = "Latitude é obrigatória")
    private Double latitude;

    @NotNull(message = "Longitude é obrigatória")
    private Double longitude;

    @NotNull(message = "Preço por kWh é obrigatório")
    private Double pricePerKwh;

    private List<String> supportedConnectors;

    private String timetable;

    @NotBlank(message = "O nome da empresa é obrigatório")
    private String companyName;

    /**
     * Workers IDs.
     */
    private List<Long> workerIds;

    /**
    * Lista de chargers a serem criados junto com a estação.
    * Cada ChargerDTO traz status e chargingSpeedKw (ID pode ser nulo na criação).
    */
    private List<ChargerDTO> chargers;

    /**
     * Constructor to create a StationCreateDTO from a Station.
     *
     * @param station the Station to convert
     */
    public StationCreateDTO(Station station) {
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

        this.chargers = List.of();
    }
}

package nikev.group.project.chargingplatform.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nikev.group.project.chargingplatform.model.Charger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChargerDTO {

    private Long id; // opcionalmente, para retornar o id quando já existir

    @NotNull(message = "Status of the charger is required")
    private Charger.ChargerStatus status;

    @NotNull(message = "Velocity of the charger is required")
    private Double chargingSpeedKw;

    public ChargerDTO(Charger charger) {
        this.id = charger.getId();
        this.status = charger.getStatus();
        this.chargingSpeedKw = charger.getChargingSpeedKw();
    }

    public Charger toCharger() {
        Charger charger = new Charger();
        charger.setId(this.id);
        charger.setStatus(this.status);
        charger.setChargingSpeedKw(this.chargingSpeedKw);
        return charger;
    }
}

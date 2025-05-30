package nikev.group.project.chargingplatform.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BookingRequestDTO {

  @NotBlank
  @NotNull
  private Long stationId;

  @NotBlank
  @NotNull
  private LocalDateTime startTime;

  @NotBlank
  @NotNull
  private LocalDateTime endTime;
}

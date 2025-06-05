package nikev.group.project.chargingplatform.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkerDTO {
    private Long id;
    private String username;
    private String email;
}

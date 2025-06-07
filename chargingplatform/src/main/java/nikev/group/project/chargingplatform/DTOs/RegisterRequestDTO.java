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
public class RegisterRequestDTO {

    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String accountType;
}

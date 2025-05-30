package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import nikev.group.project.chargingplatform.model.Role;

@Data
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    private String password;
    private String credit_card;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Ensure no List<Reservation> reservations;
    // Ensure no List<Receipt> receipts;
}
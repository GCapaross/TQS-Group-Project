package nikev.group.project.chargingplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY) // Good practice for performance
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY) // Good practice for performance
    @JoinColumn(name = "station_id")
    private Station station;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public ReservationStatus getStatus() {
        if (startDate.isAfter(LocalDateTime.now())) {
            return ReservationStatus.BOOKED;
        } else if (endDate.isBefore(LocalDateTime.now())) {
            return ReservationStatus.COMPLETED;
        } else {
            return ReservationStatus.IN_PROGRESS;
        }
    }

    public static enum ReservationStatus {
        BOOKED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
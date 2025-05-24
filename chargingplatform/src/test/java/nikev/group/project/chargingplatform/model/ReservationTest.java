package nikev.group.project.chargingplatform.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.Test;

public class ReservationTest {

  @Test
  void testChargingReservationStatusTransitions() {
    LocalDateTime now = LocalDateTime.now();
    Reservation reservation = new Reservation();

    // Given Reservation booked in the future
    reservation.setStartDate(now.plusHours(1));
    reservation.setEndDate(now.plusHours(2));

    // When get status
    // Then Reservatioon is booked
    assertThat(
      reservation.getStatus(),
      is(Reservation.ReservationStatus.BOOKED)
    );

    // Given reservation is in progress
    reservation.setStartDate(now.minusHours(1));
    reservation.setEndDate(now.plusHours(1));
    // Then status is IN_PROGRESS
    assertThat(
      reservation.getStatus(),
      is(Reservation.ReservationStatus.IN_PROGRESS)
    );

    // Given reservation already ended
    reservation.setStartDate(now.minusHours(2));
    reservation.setEndDate(now.minusHours(1));
    // Then status is COmpleted
    assertThat(
      reservation.getStatus(),
      is(Reservation.ReservationStatus.COMPLETED)
    );
  }
}

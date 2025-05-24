package nikev.group.project.chargingplatform.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    @Test
    void testChargingReservationCreation() {
        // Given
        Station station = new Station();
        station.setId(1L);
        station.setPricePerKwh(0.5);

        User user = new User();
        user.setId(1L);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStation(station);
        reservation.setUser(user);
        reservation.setStartDate(startTime);
        reservation.setEndDate(endTime);

        // Then
        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getStation()).isEqualTo(station);
        assertThat(reservation.getUser()).isEqualTo(user);
        assertThat(reservation.getStartDate()).isEqualTo(startTime);
        assertThat(reservation.getEndDate()).isEqualTo(endTime);
    }

    @Test
    void testChargingReservationStatusTransitions() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation();

        // When
        reservation.setStartDate(now.plusHours(1));
        reservation.setEndDate(now.plusHours(2));

        // Then
        assertThat(reservation.getStatus())
            .isEqualTo(Reservation.ReservationStatus.BOOKED);

        // When
        reservation.setStartDate(now.minusHours(1));
        reservation.setEndDate(now.plusHours(1));

        // Then
        assertThat(reservation.getStatus())
            .isEqualTo(Reservation.ReservationStatus.IN_PROGRESS);
        
        // When
        reservation.setStartDate(now.minusHours(2));
        reservation.setEndDate(now.minusHours(1));

        // Then
        assertThat(reservation.getStatus())
            .isEqualTo(Reservation.ReservationStatus.COMPLETED);
    }

    @Test
    void testChargingReservationEquality() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setStartDate(now.plusHours(1));
        reservation1.setEndDate(now.plusHours(2));

        Reservation reservation2 = new Reservation();
        reservation2.setId(1L);
        reservation2.setStartDate(now.plusHours(1));
        reservation2.setEndDate(now.plusHours(2));

        // Then
        assertThat(reservation1).isEqualTo(reservation2);
    }

    @Test
    void testChargingReservationDuration() {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        Reservation reservation = new Reservation();
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        // Then
        assertThat(reservation.getEndTime()).isAfter(reservation.getStartTime());
    }
}
package nikev.group.project.chargingplatform.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    @Test
    void testChargingSessionCreation() {
        // Given
        Station station = new Station();
        station.setId(1L);
        station.setPricePerKwh(0.5);

        User user = new User();
        user.setId(1L);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        Reservation session = new Reservation();
        session.setId(1L);
        session.setStation(station);
        session.setUser(user);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setEnergyConsumed(10.0);
        session.setCost(5.0);
        session.setStatus("COMPLETED");

        // Then
        assertThat(session.getId()).isEqualTo(1L);
        assertThat(session.getStation()).isEqualTo(station);
        assertThat(session.getUser()).isEqualTo(user);
        assertThat(session.getStartTime()).isEqualTo(startTime);
        assertThat(session.getEndTime()).isEqualTo(endTime);
        assertThat(session.getEnergyConsumed()).isEqualTo(10.0);
        assertThat(session.getCost()).isEqualTo(5.0);
        assertThat(session.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void testChargingSessionStatusTransitions() {
        // Given
        Reservation session = new Reservation();
        session.setStatus("BOOKED");

        // When
        session.setStatus("IN_PROGRESS");

        // Then
        assertThat(session.getStatus()).isEqualTo("IN_PROGRESS");

        // When
        session.setStatus("COMPLETED");

        // Then
        assertThat(session.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void testChargingSessionEquality() {
        // Given
        Reservation session1 = new Reservation();
        session1.setId(1L);
        session1.setStatus("COMPLETED");

        Reservation session2 = new Reservation();
        session2.setId(1L);
        session2.setStatus("COMPLETED");

        // Then
        assertThat(session1).isEqualTo(session2);
    }

    @Test
    void testChargingSessionDuration() {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        Reservation session = new Reservation();
        session.setStartTime(startTime);
        session.setEndTime(endTime);

        // Then
        assertThat(session.getEndTime()).isAfter(session.getStartTime());
    }
}
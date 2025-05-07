package nikev.group.project.chargingplatform.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ChargingSessionTest {

    @Test
    void testChargingSessionCreation() {
        // Given
        ChargingStation station = new ChargingStation();
        station.setId(1L);
        station.setPricePerKwh(0.5);

        User user = new User();
        user.setId(1L);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        ChargingSession session = new ChargingSession();
        session.setId(1L);
        session.setChargingStation(station);
        session.setUser(user);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setEnergyConsumed(10.0);
        session.setCost(5.0);
        session.setStatus("COMPLETED");

        // Then
        assertThat(session.getId()).isEqualTo(1L);
        assertThat(session.getChargingStation()).isEqualTo(station);
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
        ChargingSession session = new ChargingSession();
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
        ChargingSession session1 = new ChargingSession();
        session1.setId(1L);
        session1.setStatus("COMPLETED");

        ChargingSession session2 = new ChargingSession();
        session2.setId(1L);
        session2.setStatus("COMPLETED");

        // Then
        assertThat(session1).isEqualTo(session2);
        assertThat(session1.hashCode()).isEqualTo(session2.hashCode());
    }

    @Test
    void testChargingSessionDuration() {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        ChargingSession session = new ChargingSession();
        session.setStartTime(startTime);
        session.setEndTime(endTime);

        // Then
        assertThat(session.getEndTime()).isAfter(session.getStartTime());
    }
} 
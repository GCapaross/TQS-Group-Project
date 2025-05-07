package nikev.group.project.chargingplatform.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChargingStationTest {

    @Test
    void testChargingStationCreation() {
        // Given
        ChargingStation station = new ChargingStation();
        station.setId(1L);
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station.setMaxSlots(4);
        station.setAvailableSlots(2);
        station.setPricePerKwh(0.5);

        // Then
        assertThat(station.getId()).isEqualTo(1L);
        assertThat(station.getName()).isEqualTo("Test Station");
        assertThat(station.getLocation()).isEqualTo("Test Location");
        assertThat(station.getStatus()).isEqualTo(ChargingStation.StationStatus.AVAILABLE);
        assertThat(station.getLatitude()).isEqualTo(40.7128);
        assertThat(station.getLongitude()).isEqualTo(-74.0060);
        assertThat(station.getMaxSlots()).isEqualTo(4);
        assertThat(station.getAvailableSlots()).isEqualTo(2);
        assertThat(station.getPricePerKwh()).isEqualTo(0.5);
    }

    @Test
    void testChargingStationStatusTransitions() {
        // Given
        ChargingStation station = new ChargingStation();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);

        // When
        station.setStatus(ChargingStation.StationStatus.IN_USE);

        // Then
        assertThat(station.getStatus()).isEqualTo(ChargingStation.StationStatus.IN_USE);
    }

    @Test
    void testChargingStationEquality() {
        // Given
        ChargingStation station1 = new ChargingStation();
        station1.setId(1L);
        station1.setName("Test Station");

        ChargingStation station2 = new ChargingStation();
        station2.setId(1L);
        station2.setName("Test Station");

        // Then
        assertThat(station1).isEqualTo(station2);
        assertThat(station1.hashCode()).isEqualTo(station2.hashCode());
    }
} 
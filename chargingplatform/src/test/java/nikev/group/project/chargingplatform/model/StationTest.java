package nikev.group.project.chargingplatform.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StationTest {

    @Test
    void testChargingStationCreation() {
        // Given
        Station station = new Station();
        station.setId(1L);
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station.setPricePerKwh(0.5);
        station.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
        station.setTimetable("24/7");

        // Then
        assertThat(station.getId()).isEqualTo(1L);
        assertThat(station.getName()).isEqualTo("Test Station");
        assertThat(station.getLocation()).isEqualTo("Test Location");
        assertThat(station.getLatitude()).isEqualTo(40.7128);
        assertThat(station.getLongitude()).isEqualTo(-74.0060);
        assertThat(station.getPricePerKwh()).isEqualTo(0.5);
        assertThat(station.getSupportedConnectors()).containsExactly("CCS", "Type 2");
        assertThat(station.getTimetable()).isEqualTo("24/7");
    }

    @Test
    void testChargingStationStatusTransitions() {
        // Given
        Station station = new Station();
        Charger busy = new Charger();
        Charger free = new Charger();
        Charger charging = new Charger();
        Charger reserved = new Charger();

        // When
        busy.setStatus(Charger.ChargerStatus.CHARGING);
        free.setStatus(Charger.ChargerStatus.AVAILABLE);
        station.setChargers(List.of(busy, free));

        // Then
        assertThat(station.hasAvailableCharger()).isTrue();

        // When
        charging.setStatus(Charger.ChargerStatus.CHARGING);
        reserved.setStatus(Charger.ChargerStatus.RESERVED);
        station.setChargers(List.of(charging, reserved));

        // Then
        assertThat(station.hasAvailableCharger()).isFalse();
    }

    @Test
    void testChargingStationEquality() {
        // Given
        Station station1 = new Station();
        station1.setId(1L);
        station1.setName("Test Station");
        station1.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));

        Station station2 = new Station();
        station2.setId(1L);
        station2.setName("Test Station");
        station2.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));

        // Then
        assertThat(station1).isEqualTo(station2);
        assertThat(station1.hashCode()).isEqualTo(station2.hashCode());
    }

    /*
     * Test?
     * This test is commented out because the StationReview class is not defined.
    @Test
    void testChargingStationReviews() {
        // Given
        Station station = new Station();
        station.setId(1L);

        StationReview review1 = new StationReview();
        review1.setId(1L);
        review1.setRating(5);
        review1.setComment("Great station!");

        StationReview review2 = new StationReview();
        review2.setId(2L);
        review2.setRating(4);
        review2.setComment("Good service");

        // When
        station.setReviews(Arrays.asList(review1, review2));

        // Then
        assertThat(station.getReviews()).hasSize(2);
        assertThat(station.getReviews().get(0)).isEqualTo(review1);
        assertThat(station.getReviews().get(1)).isEqualTo(review2);
    }
    */
}
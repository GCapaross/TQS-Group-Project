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
        station.setName("Test Station");
        station.setLocation("Test Location");

        // When
        station.setStatus("IN_USE");

        // Then
        assertThat(station.getStatus()).isEqualTo("IN_USE");
    }

    @Test
    void testChargingStationEquality() {
        // Given
        Station station1 = new Station();
        station1.setId(1L);
        station1.setName("Test Station");
        station1.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
        station1.setChargingSpeedKw(50.0);
        station1.setCarrierNetwork("Test Network");

        Station station2 = new Station();
        station2.setId(1L);
        station2.setName("Test Station");
        station2.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
        station2.setChargingSpeedKw(50.0);
        station2.setCarrierNetwork("Test Network");

        // Then
        assertThat(station1).isEqualTo(station2);
        assertThat(station1.hashCode()).isEqualTo(station2.hashCode());
    }

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
}
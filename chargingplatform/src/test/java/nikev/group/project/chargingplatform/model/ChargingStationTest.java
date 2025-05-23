package nikev.group.project.chargingplatform.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChargingStationTest {

    @Test
    void testChargingStationCreation() {
        // Given
        Station station = new Station();
        station.setId(1L);
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(Station.StationStatus.AVAILABLE);
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station.setMaxSlots(4);
        station.setAvailableSlots(2);
        station.setPricePerKwh(0.5);
        station.setConnectorTypes(Arrays.asList("CCS", "Type 2"));
        station.setChargingSpeedKw(50.0);
        station.setCarrierNetwork("Test Network");
        station.setAverageRating(4.5);

        // Then
        assertThat(station.getId()).isEqualTo(1L);
        assertThat(station.getName()).isEqualTo("Test Station");
        assertThat(station.getLocation()).isEqualTo("Test Location");
        assertThat(station.getStatus()).isEqualTo(Station.StationStatus.AVAILABLE);
        assertThat(station.getLatitude()).isEqualTo(40.7128);
        assertThat(station.getLongitude()).isEqualTo(-74.0060);
        assertThat(station.getMaxSlots()).isEqualTo(4);
        assertThat(station.getAvailableSlots()).isEqualTo(2);
        assertThat(station.getPricePerKwh()).isEqualTo(0.5);
        assertThat(station.getConnectorTypes()).containsExactly("CCS", "Type 2");
        assertThat(station.getChargingSpeedKw()).isEqualTo(50.0);
        assertThat(station.getCarrierNetwork()).isEqualTo("Test Network");
        assertThat(station.getAverageRating()).isEqualTo(4.5);
    }

    @Test
    void testChargingStationStatusTransitions() {
        // Given
        Station station = new Station();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(Station.StationStatus.AVAILABLE);

        // When
        station.setStatus(Station.StationStatus.IN_USE);

        // Then
        assertThat(station.getStatus()).isEqualTo(Station.StationStatus.IN_USE);
    }

    @Test
    void testChargingStationEquality() {
        // Given
        Station station1 = new Station();
        station1.setId(1L);
        station1.setName("Test Station");
        station1.setConnectorTypes(Arrays.asList("CCS", "Type 2"));
        station1.setChargingSpeedKw(50.0);
        station1.setCarrierNetwork("Test Network");

        Station station2 = new Station();
        station2.setId(1L);
        station2.setName("Test Station");
        station2.setConnectorTypes(Arrays.asList("CCS", "Type 2"));
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
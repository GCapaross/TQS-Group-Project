package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.ChargingStation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class ChargingStationRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        ChargingStation station = new ChargingStation();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station.setMaxSlots(4);
        station.setAvailableSlots(2);
        station.setPricePerKwh(0.5);
        station.setConnectorTypes(Arrays.asList("CCS", "Type 2"));
        station.setChargingSpeedKw(50.0);
        station.setCarrierNetwork("Test Network");
        station.setAverageRating(4.5);

        // When
        ChargingStation saved = chargingStationRepository.save(station);
        Optional<ChargingStation> found = chargingStationRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Station");
        assertThat(found.get().getLocation()).isEqualTo("Test Location");
        assertThat(found.get().getStatus()).isEqualTo(ChargingStation.StationStatus.AVAILABLE);
        assertThat(found.get().getConnectorTypes()).containsExactly("CCS", "Type 2");
        assertThat(found.get().getChargingSpeedKw()).isEqualTo(50.0);
        assertThat(found.get().getCarrierNetwork()).isEqualTo("Test Network");
        assertThat(found.get().getAverageRating()).isEqualTo(4.5);
    }

    @Test
    void testFindAll() {
        // Given
        ChargingStation station1 = new ChargingStation();
        station1.setName("Station 1");
        station1.setLocation("Location 1");
        station1.setStatus(ChargingStation.StationStatus.AVAILABLE);
        station1.setConnectorTypes(Arrays.asList("CCS"));
        station1.setChargingSpeedKw(50.0);
        chargingStationRepository.save(station1);

        // When
        List<ChargingStation> stations = chargingStationRepository.findAll();

        // Then
        assertThat(stations).hasSize(1);
        assertThat(stations.get(0).getName()).isEqualTo("Station 1");
    }

    @Test
    void testDelete() {
        // Given
        ChargingStation station = new ChargingStation();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        ChargingStation saved = chargingStationRepository.save(station);

        // When
        chargingStationRepository.delete(saved);
        Optional<ChargingStation> found = chargingStationRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testFindNearbyStations() {
        // Given
        ChargingStation station = new ChargingStation();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        chargingStationRepository.save(station);

        // When
        Specification<ChargingStation> spec = (root, query, cb) -> {
            double radiusDegrees = 10.0 / 111.0;
            return cb.and(
                cb.between(root.get("latitude"), 40.7128 - radiusDegrees, 40.7128 + radiusDegrees),
                cb.between(root.get("longitude"), -74.0060 - radiusDegrees, -74.0060 + radiusDegrees)
            );
        };
        List<ChargingStation> nearbyStations = chargingStationRepository.findAll(spec);

        // Then
        assertThat(nearbyStations).hasSize(1);
        assertThat(nearbyStations.get(0).getName()).isEqualTo("Test Station");
    }

    @Test
    void testFindStationsWithFilters() {
        // Given
        ChargingStation station = new ChargingStation();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        station.setConnectorTypes(Arrays.asList("CCS", "Type 2"));
        station.setChargingSpeedKw(50.0);
        station.setCarrierNetwork("Test Network");
        station.setAverageRating(4.5);
        chargingStationRepository.save(station);

        // When
        Specification<ChargingStation> spec = (root, query, cb) -> {
            return cb.and(
                cb.isMember("CCS", root.get("connectorTypes")),
                cb.greaterThanOrEqualTo(root.get("chargingSpeedKw"), 50.0),
                cb.equal(root.get("carrierNetwork"), "Test Network"),
                cb.greaterThanOrEqualTo(root.get("averageRating"), 4.0)
            );
        };
        List<ChargingStation> filteredStations = chargingStationRepository.findAll(spec);

        // Then
        assertThat(filteredStations).hasSize(1);
        assertThat(filteredStations.get(0).getName()).isEqualTo("Test Station");
    }
} 
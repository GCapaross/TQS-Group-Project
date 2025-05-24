package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.repository.StationRepository;
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
class StationRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private StationRepository stationRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Station station = new Station();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station.setPricePerKwh(0.5);
        station.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
        station.setTimetable("24/7");

        // When
        Station saved = stationRepository.save(station);
        Optional<Station> found = stationRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Station");
        assertThat(found.get().getLocation()).isEqualTo("Test Location");
    }

    @Test
    void testFindAll() {
        // Given
        Station station1 = new Station();
        station1.setName("Station 1");
        station1.setLocation("Location 1");
        station1.setLatitude(10.0);
        station1.setLongitude(20.0);
        station1.setPricePerKwh(0.25);
        station1.setSupportedConnectors(List.of("CCS"));
        stationRepository.save(station1);

        // When
        List<Station> stations = stationRepository.findAll();

        // Then
        assertThat(stations).hasSize(1);
        Station found = stations.get(0);
        assertThat(found.getName()).isEqualTo("Station 1");
        assertThat(found.getLocation()).isEqualTo("Location 1");
        assertThat(found.getPricePerKwh()).isEqualTo(0.25);
    }

    @Test
    void testDelete() {
        // Given
        Station station = new Station();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setLatitude(10.0);
        station.setLongitude(20.0);
        station.setPricePerKwh(0.25);
        station.setSupportedConnectors(List.of("CCS"));
        Station saved = stationRepository.save(station);

        // When
        stationRepository.delete(saved);
        Optional<Station> found = stationRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testFindNearbyStations() {
        // Given
        Station station = new Station();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        stationRepository.save(station);

        // When
        Specification<Station> spec = (root, query, cb) -> {
            double radiusDegrees = 10.0 / 111.0;
            return cb.and(
                cb.between(root.get("latitude"), 40.7128 - radiusDegrees, 40.7128 + radiusDegrees),
                cb.between(root.get("longitude"), -74.0060 - radiusDegrees, -74.0060 + radiusDegrees)
            );
        };
        List<Station> nearbyStations = stationRepository.findAll(spec);

        // Then
        assertThat(nearbyStations).hasSize(1);
        assertThat(nearbyStations.get(0).getName()).isEqualTo("Test Station");
    }

    @Test
    void testFindStationsWithFilters() {
        // Given
        Station station = new Station();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setLatitude(0.0);
        station.setLongitude(0.0);
        station.setPricePerKwh(0.30);
        station.setSupportedConnectors(List.of("CCS", "Type 2"));
        stationRepository.save(station);

        // When
        Specification<Station> spec = (root, query, cb) -> cb.and(
            cb.isMember("CCS", root.get("supportedConnectors")),
            cb.lessThanOrEqualTo(root.get("pricePerKwh"), 0.50),
            cb.equal(root.get("location"), "Test Location")
        );
        List<Station> filteredStations = stationRepository.findAll(spec);

        // Then
        assertThat(filteredStations).hasSize(1);
        assertThat(filteredStations.get(0).getName()).isEqualTo("Test Station");
    }
}
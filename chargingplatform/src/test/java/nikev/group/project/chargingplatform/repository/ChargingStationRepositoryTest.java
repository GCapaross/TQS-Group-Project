package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.ChargingStation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

        // When
        ChargingStation saved = chargingStationRepository.save(station);
        Optional<ChargingStation> found = chargingStationRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Station");
        assertThat(found.get().getLocation()).isEqualTo("Test Location");
        assertThat(found.get().getStatus()).isEqualTo(ChargingStation.StationStatus.AVAILABLE);
    }

    @Test
    void testFindAll() {
        // Given
        ChargingStation station1 = new ChargingStation();
        station1.setName("Station 1");
        station1.setLocation("Location 1");
        station1.setStatus(ChargingStation.StationStatus.AVAILABLE);
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
} 
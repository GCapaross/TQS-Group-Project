package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class ChargingSessionRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ChargingSessionRepository chargingSessionRepository;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private UserRepository userRepository;

    private Station station;
    private User user;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        station = new Station();
        station.setName("Test Station");
        station.setLocation("Test Location");
        station.setStatus(Station.StationStatus.AVAILABLE);
        station = chargingStationRepository.save(station);

        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword123");
        user = userRepository.save(user);

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void testSaveAndFindById() {
        // Given
        ChargingSession session = new ChargingSession();
        session.setChargingStation(station);
        session.setUser(user);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setStatus("BOOKED");

        // When
        ChargingSession saved = chargingSessionRepository.save(session);
        Optional<ChargingSession> found = chargingSessionRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("BOOKED");
        assertThat(found.get().getChargingStation()).isEqualTo(station);
        assertThat(found.get().getUser()).isEqualTo(user);
    }

    @Test
    void testFindByUserId() {
        // Given
        ChargingSession session = new ChargingSession();
        session.setChargingStation(station);
        session.setUser(user);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setStatus("BOOKED");
        chargingSessionRepository.save(session);

        // When
        List<ChargingSession> sessions = chargingSessionRepository.findByUserId(user.getId());

        // Then
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getUser()).isEqualTo(user);
    }

    @Test
    void testFindByChargingStationId() {
        // Given
        ChargingSession session = new ChargingSession();
        session.setChargingStation(station);
        session.setUser(user);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setStatus("BOOKED");
        chargingSessionRepository.save(session);

        // When
        List<ChargingSession> sessions = chargingSessionRepository.findByChargingStationId(station.getId());

        // Then
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getChargingStation()).isEqualTo(station);
    }

    @Test
    void testFindOverlappingSessions() {
        // Given
        ChargingSession existingSession = new ChargingSession();
        existingSession.setChargingStation(station);
        existingSession.setUser(user);
        existingSession.setStartTime(startTime);
        existingSession.setEndTime(endTime);
        existingSession.setStatus("BOOKED");
        chargingSessionRepository.save(existingSession);

        // When
        List<ChargingSession> overlapping = chargingSessionRepository.findOverlappingSessions(
                station.getId(),
                startTime.minusMinutes(30),
                endTime.minusMinutes(30)
        );

        // Then
        assertThat(overlapping).hasSize(1);
        assertThat(overlapping.get(0)).isEqualTo(existingSession);
    }
} 
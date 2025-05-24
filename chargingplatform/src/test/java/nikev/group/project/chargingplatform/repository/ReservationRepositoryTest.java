package nikev.group.project.chargingplatform.repository;

import nikev.group.project.chargingplatform.model.Reservation;
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
class ReservationRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private StationRepository stationRepository;

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
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station = stationRepository.save(station);

        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void testSaveAndFindById() {
        // Given
        Reservation reservation = new Reservation();
        reservation.setStation(station);
        reservation.setUser(user);
        reservation.setStartDate(startTime);
        reservation.setEndDate(endTime);

        // When
        Reservation saved = reservationRepository.save(reservation);
        Optional<Reservation> found = reservationRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isEqualTo(user);
    }

    @Test
    void testFindByUserId() {
        // Given
        Reservation reservation = new Reservation();
        reservation.setStation(station);
        reservation.setUser(user);
        reservation.setStartDate(startTime);
        reservation.setEndDate(endTime);
        reservationRepository.save(reservation);

        // When
        List<Reservation> reservations = reservationRepository.findByUserId(user.getId());

        // Then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getUser()).isEqualTo(user);
    }

    @Test
    void testFindByChargingStationId() {
        // Given
        Reservation reservation = new Reservation();
        reservation.setStation(station);
        reservation.setUser(user);
        reservation.setStartDate(startTime);
        reservation.setEndDate(endTime);
        reservationRepository.save(reservation);

        // When
        List<Reservation> reservations = reservationRepository.findByChargingStationId(station.getId());

        // Then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getStation()).isEqualTo(station);
    }

    @Test
    void testFindOverlappingReservations() {
        // Given
        Reservation existingReservation = new Reservation();
        existingReservation.setStation(station);
        existingReservation.setUser(user);
        existingReservation.setStartDate(startTime);
        existingReservation.setEndDate(endTime);
        reservationRepository.save(existingReservation);

        // When
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                station.getId(),
                startTime.minusMinutes(30),
                endTime.minusMinutes(30)
        );

        // Then
        assertThat(overlapping).hasSize(1);
        assertThat(overlapping.get(0)).isEqualTo(existingReservation);
    }
}
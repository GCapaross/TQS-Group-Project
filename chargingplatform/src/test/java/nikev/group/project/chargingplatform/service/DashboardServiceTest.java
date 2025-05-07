package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ChargingSessionRepository chargingSessionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private ChargingSession session1;
    private ChargingSession session2;
    private ChargingStation station;
    private User user;

    @BeforeEach
    void setUp() {
        station = new ChargingStation();
        station.setId(1L);

        user = new User();
        user.setId(1L);

        session1 = new ChargingSession();
        session1.setId(1L);
        session1.setChargingStation(station);
        session1.setUser(user);
        session1.setEnergyConsumed(10.0);
        session1.setCost(5.0);
        session1.setStatus("COMPLETED");

        session2 = new ChargingSession();
        session2.setId(2L);
        session2.setChargingStation(station);
        session2.setUser(user);
        session2.setEnergyConsumed(15.0);
        session2.setCost(7.5);
        session2.setStatus("COMPLETED");
    }

    @Test
    void getUserConsumptionStats_ShouldReturnCorrectStats() {
        // Given
        List<ChargingSession> sessions = Arrays.asList(session1, session2);
        when(chargingSessionRepository.findByUserId(1L)).thenReturn(sessions);

        // When
        Map<String, Object> stats = dashboardService.getUserConsumptionStats(1L);

        // Then
        assertThat(stats.get("totalEnergyConsumed")).isEqualTo(25.0);
        assertThat(stats.get("totalCost")).isEqualTo(12.5);
        assertThat(stats.get("totalSessions")).isEqualTo(2L);
        assertThat((List<ChargingSession>) stats.get("sessions")).hasSize(2);
    }

    @Test
    void getStationStats_ShouldReturnCorrectStats() {
        // Given
        List<ChargingSession> sessions = Arrays.asList(session1, session2);
        when(chargingSessionRepository.findByChargingStationId(1L)).thenReturn(sessions);

        // When
        Map<String, Object> stats = dashboardService.getStationStats(1L);

        // Then
        assertThat(stats.get("totalEnergyDelivered")).isEqualTo(25.0);
        assertThat(stats.get("totalRevenue")).isEqualTo(12.5);
        assertThat(stats.get("totalSessions")).isEqualTo(2L);
        assertThat((List<ChargingSession>) stats.get("sessions")).hasSize(2);
    }

    @Test
    void getAggregateStats_ShouldReturnCorrectStats() {
        // Given
        List<ChargingSession> allSessions = Arrays.asList(session1, session2);
        when(chargingSessionRepository.findAll()).thenReturn(allSessions);

        // When
        Map<String, Object> stats = dashboardService.getAggregateStats();

        // Then
        assertThat(stats.get("totalEnergy")).isEqualTo(25.0);
        assertThat(stats.get("totalRevenue")).isEqualTo(12.5);
        Map<String, Long> sessionsByStatus = (Map<String, Long>) stats.get("sessionsByStatus");
        assertThat(sessionsByStatus.get("COMPLETED")).isEqualTo(2L);
    }
} 
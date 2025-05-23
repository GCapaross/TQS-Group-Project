package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.ChargingSession;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.repository.ChargingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ChargingSessionRepository chargingSessionRepository;

    @InjectMocks
    private PaymentService paymentService;

    private ChargingSession session;
    private Station station;

    @BeforeEach
    void setUp() {
        station = new Station();
        station.setPricePerKwh(0.5);

        session = new ChargingSession();
        session.setChargingStation(station);
        session.setEnergyConsumed(10.0);
    }

    @Test
    void processPayment_ShouldCalculateCostAndUpdateSession() {
        // Given
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When
        String transactionId = paymentService.processPayment(session);

        // Then
        assertThat(transactionId).isNotNull();
        assertThat(session.getCost()).isEqualTo(5.0); // 10.0 kWh * 0.5 price
        assertThat(session.getStatus()).isEqualTo("PAID");
        verify(chargingSessionRepository).save(session);
    }

    @Test
    void refundPayment_ShouldUpdateSessionStatus() {
        // Given
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When
        paymentService.refundPayment(session);

        // Then
        assertThat(session.getStatus()).isEqualTo("REFUNDED");
        verify(chargingSessionRepository).save(session);
    }
} 
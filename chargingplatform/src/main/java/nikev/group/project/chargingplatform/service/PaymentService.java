package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.repository.ChargingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private ChargingSessionRepository chargingSessionRepository;

    @Transactional
    public String processPayment(Reservation session) {
        // In a real implementation, this would integrate with a payment gateway
        // For MVP, we'll simulate a successful payment
        
        double amount = calculateAmount(session);
        String transactionId = UUID.randomUUID().toString();
        
        session.setCost(amount);
        session.setStatus("PAID");
        chargingSessionRepository.save(session);
        
        return transactionId;
    }

    private double calculateAmount(Reservation session) {
        // Calculate amount based on energy consumed and station's price per kWh
        double energyConsumed = session.getEnergyConsumed();
        double pricePerKwh = session.getChargingStation().getPricePerKwh();
        
        return energyConsumed * pricePerKwh;
    }

    @Transactional
    public void refundPayment(Reservation session) {
        // In a real implementation, this would integrate with a payment gateway
        // For MVP, we'll simulate a successful refund
        
        session.setStatus("REFUNDED");
        chargingSessionRepository.save(session);
    }
} 
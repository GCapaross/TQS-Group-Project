package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargingSessionRepository;
import nikev.group.project.chargingplatform.repository.ChargingStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private ChargingSessionRepository chargingSessionRepository;
    
    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Transactional
    public Reservation bookSlot(Long stationId, Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Station station = chargingStationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Charging station not found"));
        
        if (station.getStatus() != Station.StationStatus.AVAILABLE) {
            throw new RuntimeException("Station is not available for booking");
        }
        
        if (station.getAvailableSlots() <= 0) {
            throw new RuntimeException("No available slots at this station");
        }
        
        // Check for overlapping bookings
        List<Reservation> overlappingSessions = chargingSessionRepository
                .findOverlappingSessions(stationId, startTime, endTime);
        
        if (!overlappingSessions.isEmpty()) {
            throw new RuntimeException("Time slot is already booked");
        }
        
        Reservation session = new Reservation();
        session.setChargingStation(station);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setStatus("BOOKED");
        
        station.setAvailableSlots(station.getAvailableSlots() - 1);
        if (station.getAvailableSlots() == 0) {
            station.setStatus(Station.StationStatus.IN_USE);
        }
        
        chargingStationRepository.save(station);
        return chargingSessionRepository.save(session);
    }

    @Transactional
    public void cancelBooking(Long sessionId) {
        Reservation session = chargingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Station station = session.getChargingStation();
        station.setAvailableSlots(station.getAvailableSlots() + 1);
        
        if (station.getStatus() == Station.StationStatus.IN_USE) {
            station.setStatus(Station.StationStatus.AVAILABLE);
        }
        
        chargingStationRepository.save(station);
        chargingSessionRepository.delete(session);
    }

    public Reservation getBooking(Long sessionId) {
        return chargingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
} 
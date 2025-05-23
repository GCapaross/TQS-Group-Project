package nikev.group.project.chargingplatform.service;

import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StationRepository stationRepository;

    @Transactional
    public Reservation bookSlot(Long stationId, Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Charging station not found"));
                
        if (station.hasAvailableCharger()) {
            throw new RuntimeException("No available slots at this station");
        }
        
        // Check for overlapping bookings
        List<Reservation> overlappingReservations = reservationRepository
                .findOverlappingReservations(stationId, startTime, endTime);
        
        if (!overlappingReservations.isEmpty()) {
            throw new RuntimeException("Time slot is already booked");
        }
        
        // Associate user and station in reservation
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setStation(station);
        reservation.setStartDate(startTime);
        reservation.setEndDate(endTime);
        
        // Persist reservation
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelBooking(Long sessionId) {
        Reservation reservation = reservationRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        // Delete reservation
        reservationRepository.delete(reservation);
    }

    public Reservation getBooking(Long sessionId) {
        return reservationRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
}
package nikev.group.project.chargingplatform.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private StationRepository stationRepository;

  @Autowired
  private ChargerRepository chargerRepository; // Added ChargerRepository

  @Transactional
  public Reservation bookSlot(
    Long stationId,
    Long userId,
    LocalDateTime startTime,
    LocalDateTime endTime
  ) {
    // Station station = stationRepository.findById(stationId) // original line
    //         .orElseThrow(() -> new RuntimeException("Charging station not found"));

    // if (station.hasAvailableCharger()) { // original line
    //     throw new RuntimeException("No available slots at this station");
    // }

    // Updated logic to use hasAvailableCharger service method
    Station station = stationRepository
      .findById(stationId)
      .orElseThrow(() -> new RuntimeException("Charging station not found"));

    if (!hasAvailableCharger(stationId)) { // Use the new service method
      throw new RuntimeException("No available slots at this station");
    }

    // Check for overlapping bookings
    List<Reservation> overlappingReservations =
      reservationRepository.findOverlappingReservations(
        stationId,
        startTime,
        endTime
      );

    if (!overlappingReservations.isEmpty()) {
      throw new RuntimeException("Time slot is already booked");
    }

    // Associate user and station in reservation
    User user = userRepository
      .findById(userId)
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
    Reservation reservation = reservationRepository
      .findById(sessionId)
      .orElseThrow(() -> new RuntimeException("Booking not found"));
    // Delete reservation
    reservationRepository.delete(reservation);
  }

  public Reservation getBooking(Long sessionId) {
    return reservationRepository
      .findById(sessionId)
      .orElseThrow(() -> new RuntimeException("Booking not found"));
  }

  // New method to check for available chargers
  public boolean hasAvailableCharger(Long stationId) {
    List<Charger> chargers = chargerRepository.findByStationId(stationId);
    if (chargers == null || chargers.isEmpty()) {
      return false;
    }
    return chargers
      .stream()
      .anyMatch(c -> c.getStatus() == Charger.ChargerStatus.AVAILABLE);
  }

  /**
   * Checks if there are available slots to create a new booking given the overlapping reservations and max capacity
   * @param overlappingReservations
   * @param maxCapacity
   * @return boolean
   */
  public boolean hasAvailableSlot(
    List<Reservation> overlappingReservations,
    int maxCapacity
  ) {
    // If there are less reservations than the maximum capacity, then it a spot is always guaranteed
    if (overlappingReservations.size() < maxCapacity) {
      return true;
    }

    int usedSpots = getMaximumChargersUsedAtSameTime(overlappingReservations);
    return (usedSpots < maxCapacity);
  }

  /**
   * Auxiliar class to aid in the checkSlotAvailability algorithm
   */
  @AllArgsConstructor
  @Getter
  class ReservationAction {

    int type; // ENTER = 1; LEAVE = -1
    LocalDateTime date;
  }

  /**
   * Calculates how many chargers are used at the same time by the overlapping reservations
   *
   * @param overlappingReservations
   * @param maxCapacity
   * @return int that represents the maximum number of chargers used at the same time
   */
  public int getMaximumChargersUsedAtSameTime(
    List<Reservation> overlappingReservations
  ) {
    List<ReservationAction> reservationActions = new ArrayList<>();
    for (Reservation reservation : overlappingReservations) {
      ReservationAction enter = new ReservationAction(
        1,
        reservation.getStartDate()
      );
      ReservationAction leave = new ReservationAction(
        -1,
        reservation.getEndDate()
      );
      reservationActions.add(enter);
      reservationActions.add(leave);
    }
    reservationActions.sort((r1, r2) -> r1.getDate().compareTo(r2.getDate()));

    int usersAtSameTime = 0;
    int maxUsersAtSameTime = 0;
    for (ReservationAction action : reservationActions) {
      usersAtSameTime += action.getType();
      if (maxUsersAtSameTime < usersAtSameTime) {
        maxUsersAtSameTime = usersAtSameTime;
      }
    }

    return maxUsersAtSameTime;
  }
}

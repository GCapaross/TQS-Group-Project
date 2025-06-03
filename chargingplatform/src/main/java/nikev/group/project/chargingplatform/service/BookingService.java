package nikev.group.project.chargingplatform.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    Station station = stationRepository
      .findById(stationId)
      .orElseThrow(() -> new RuntimeException("Charging station not found"));

    // Check for overlapping bookings
    List<Reservation> overlappingReservations =
      reservationRepository.findOverlappingReservations(
        stationId,
        startTime,
        endTime
      );
    System.out.println(
      "Overlapping reservations: " + overlappingReservations.size()
    );

    if (
      !hasAvailableSlot(
        overlappingReservations,
        chargerRepository.findByStation_Id(stationId).size()
      )
    ) {
      System.out.println("No available slots for this station");
      throw new RuntimeException("No available slots for this station");
    }

    System.out.println("No available slots for this station");
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
    List<Charger> chargers = chargerRepository.findByStation_Id(stationId);
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
    reservationActions.sort((r1, r2) -> {
      int compare = r1.getDate().compareTo(r2.getDate());
      if (compare != 0) {
        return compare;
      }

      // If dates are equal, then leave must come first!
      if (r1.getType() < r2.getType()) {
        return -1;
      }
      return 1; // Equals doesnt matter, just that leave should always appear first if dates are equals
    });

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

  @Transactional
  public Reservation startLiveChargingSession(
    Long stationId,
    Long userId,
    double initialBatteryLevel,
    double targetBatteryLevel
  ) {
    System.out.println("Starting live charging session for station " + stationId + " and user " + userId);
    
    Station station = stationRepository
      .findById(stationId)
      .orElseThrow(() -> new RuntimeException("Charging station not found"));

    // Get available charger
    List<Charger> chargers = chargerRepository.findByStation_Id(stationId);
    if (chargers == null || chargers.isEmpty()) {
      throw new RuntimeException("No chargers found for station");
    }

    Charger availableCharger = chargers.stream()
      .filter(c -> c.getStatus() == Charger.ChargerStatus.AVAILABLE)
      .findFirst()
      .orElseThrow(() -> new RuntimeException("No available chargers"));

    System.out.println("Found available charger: " + availableCharger.getId());

    // Calculate end time based on battery levels and charging speed
    LocalDateTime startTime = LocalDateTime.now();
    double batteryToCharge = targetBatteryLevel - initialBatteryLevel;
    double batteryCapacity = 75.0; // kWh (typical EV battery)
    double energyNeeded = batteryToCharge * batteryCapacity / 100.0;
    double hoursToCharge = energyNeeded / availableCharger.getChargingSpeedKw();
    LocalDateTime endTime = startTime.plusSeconds((long) (hoursToCharge * 3600));

    System.out.println("Calculated charging time: " + hoursToCharge + " hours");

    // Check for overlapping bookings
    List<Reservation> overlappingReservations =
      reservationRepository.findOverlappingReservations(
        stationId,
        startTime,
        endTime
      );

    System.out.println("Found " + overlappingReservations.size() + " overlapping reservations");

    if (
      !hasAvailableSlot(
        overlappingReservations,
        chargers.size()
      )
    ) {
      throw new RuntimeException("No available slots for this station");
    }

    // Create booking
    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));
    Reservation reservation = new Reservation();
    reservation.setUser(user);
    reservation.setStation(station);
    reservation.setStartDate(startTime);
    reservation.setEndDate(endTime);

    // Update charger status
    availableCharger.setStatus(Charger.ChargerStatus.CHARGING);
    chargerRepository.save(availableCharger);

    System.out.println("Created reservation and updated charger status");

    // Save reservation
    return reservationRepository.save(reservation);
  }

  @Transactional
  public Reservation stopLiveChargingSession(Long reservationId) {
    Reservation reservation = reservationRepository
      .findById(reservationId)
      .orElseThrow(() -> new RuntimeException("Booking not found"));

    // Update end time to current time
    reservation.setEndDate(LocalDateTime.now());

    // Find and update charger status
    List<Charger> chargers = chargerRepository.findByStation_Id(reservation.getStation().getId());
    chargers.stream()
      .filter(c -> c.getStatus() == Charger.ChargerStatus.CHARGING)
      .findFirst()
      .ifPresent(charger -> {
        charger.setStatus(Charger.ChargerStatus.AVAILABLE);
        chargerRepository.save(charger);
      });

    return reservationRepository.save(reservation);
  }
}

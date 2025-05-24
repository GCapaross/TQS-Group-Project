package nikev.group.project.chargingplatform.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class BookingServiceTest {

  @Mock
  private ReservationRepository reservationRepository;

  @Mock
  private StationRepository chargingStationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private StationRepository stationRepository;

  @InjectMocks
  private BookingService bookingService;

  private Station station;
  Charger stationCharger;
  private User user;
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  @BeforeEach
  void setUp() {
    // Setup test data
    station = new Station();
    station.setId(1L);
    station.setName("Test Station");
    station.setLocation("Test Location");
    station.setLatitude(40.7128);
    station.setLongitude(-74.0060);
    station.setPricePerKwh(0.5);
    station.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
    station.setTimetable("24/7");
    stationCharger = new Charger(
      111L,
      Charger.ChargerStatus.MAINTENANCE,
      1.45,
      station
    );

    user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setEmail("test@example.com");

    startTime = LocalDateTime.now().plusHours(1);
    endTime = startTime.plusHours(2);
  }

  @Test
  void whenBookingValidSlot_thenBookingIsCreated() {
    // Arrange
    when(chargingStationRepository.findById(1L)).thenReturn(
      Optional.of(station)
    );
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    Long stationId = 1L;
    when(
      reservationRepository.findOverlappingReservations(any(), any(), any())
    ).thenReturn(new ArrayList<>());
    when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]
    );

    // Act
    Reservation reservation = bookingService.bookSlot(
      1L,
      1L,
      startTime,
      endTime
    );

    // Assert
    assertNotNull(reservation);
    assertEquals(Reservation.ReservationStatus.BOOKED, reservation.getStatus());
    assertEquals(station, reservation.getStation());
    assertEquals(startTime, reservation.getStartDate());
    assertEquals(endTime, reservation.getEndDate());
    verify(reservationRepository).save(any(Reservation.class));
  }

  @Test
  void whenBookingOverlappingSlot_thenThrowsException() {
    // Arrange
    when(chargingStationRepository.findById(1L)).thenReturn(
      Optional.of(station)
    );
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    List<Reservation> overlappingSessions = new ArrayList<>();
    Reservation existingBooking = new Reservation();
    overlappingSessions.add(existingBooking);
    when(
      reservationRepository.findOverlappingReservations(any(), any(), any())
    ).thenReturn(overlappingSessions);

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      bookingService.bookSlot(1L, 1L, startTime, endTime)
    );
  }

  @Test
  void whenStationNotAvailable_thenThrowsException() {
    // Arrange
    when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act & Assert
    assertThrows(RuntimeException.class, () ->
      bookingService.bookSlot(1L, 1L, startTime, endTime)
    );
  }

  @Test
  void whenNoAvailableSlots_thenThrowsException() {
    // Arrange
    when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      bookingService.bookSlot(1L, 1L, startTime, endTime)
    );
    assertNotNull(exception.getMessage());
  }

  @Test
  void whenBookingLastSlot_thenStationStatusChangesToInUse() {
    // Arrange
    Charger charger = new Charger();
    charger.setId(111L);
    charger.setStatus(Charger.ChargerStatus.AVAILABLE);
    charger.setChargingSpeedKw(1.45);
    station.setChargers(List.of(charger));

    when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    when(
      reservationRepository.findOverlappingReservations(any(), any(), any())
    ).thenReturn(Collections.emptyList());
    when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]
    );
    when(stationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

    // Act
    bookingService.bookSlot(1L, 1L, startTime, endTime);

    // Assert
    assertThat(charger.getStatus()).isEqualTo(Charger.ChargerStatus.CHARGING);
    assertThat(station.hasAvailableCharger()).isFalse();
  }

  @Test
  void whenCancellingBooking_thenSlotIsReleased() {
    // Arrange
    Reservation booking = new Reservation();
    booking.setId(1L);
    station.setChargers(
      List.of(new Charger(111L, Charger.ChargerStatus.RESERVED, 1.45))
    );
    booking.setStation(station);

    when(reservationRepository.findById(1L)).thenReturn(Optional.of(booking));
    when(chargingStationRepository.save(any())).thenAnswer(i ->
      i.getArguments()[0]
    );

    // Act
    bookingService.cancelBooking(1L);

    // Assert
    assertThat(station.hasAvailableCharger()).isTrue();
    verify(reservationRepository).delete(booking);
  }
  /**
   * Given no booking with id 5
   * When trying to cancel booking with id 5
   * then RuntimeException is thrown
   */
  /**
   * Given booking with id 5
   * When trying to cancel booking with id 5
   * then booking is no longer in the system
   */
  //   public void cancelBooking(Long sessionId) {

  /**
   * Given no station with id 5
   * When verify it has available charger
   * then RuntimeException is thrown
   */
  /**
   * Given no station with id 5 and 2 chargers with one with status available
   * When verify it has available charger
   * then return true
   */
  /**
   * Given no station with id 5 and 2 chargers both with status different from available
   * When verify it has available charger
   * then return false
   */
  //   public boolean hasAvailableCharger(Long stationId) {

  /**
   * Given no reservation with id 5
   * When fetching booking
   * then RuntimeException is thrown
   */
  /**
   * Given a reservation with id 5 associated to user with id 2
   * When user is user with id 2
   * then return reservation
   */
  /**
   * Given a reservation with id 5 associated to user with id 2
   * When user is not user with id 2 and doesnt has role manager for the station with id 5
   * then RuntimeException is thrown
   */
  /**
   * Given a reservation with id 5 associated to user with id 2
   * When user is not user with id 2 and has role manager for the station with id 3
   * then RuntimeException is thrown
   */
  /**
   * Given a reservation with id 5 associated to user with id 2
   * When user is not user with id 2 and has role manager for the station with id 5
   * then reservation is returned
   */
  //   public Reservation getBooking(Long sessionId) {

  /**
   * (Unexisten station)
   * Given no station with id 5
   * When booking a slot to station with id 5
   * then throw RuntimeException
   */
  /**
   * (Unexistent user)
   * Given station with id 5 and no user with id 1
   * When booking a slot to station with id 5 by the user with id 1
   * then throw RuntimeException
   */
  /**
   * (No slots available)
   * Given station with 2 chargers and 2 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h30
   * then throw RuntimeException
   */
  /**
   * (Charger out of service)
   * Given station with 2 chargers and 1 reservation between 14h30 and 15h30
   *    but 1 charger has state OUT_OF_SERVICE
   * When booking a slot to the station to the slot 14h30 and 15h00
   * then throw RuntimeException
   */
  /**
   * (Free station)
   * Given station with 2 chargers and 2 reservations:
   *    one between 14h30 and 15h00 and another between 15h00 and 15h30
   * When booking a slot to the station to the slot 14h45 and 15h15
   * then Reservation is made
   */
  /**
   * Given station with 2 chargers and 1 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h00
   * then Reservation is made
   */
  //   @Transactional
  //   public Reservation bookSlot(
  //     Long stationId,
  //     Long userId,
  //     LocalDateTime startTime,
  //     LocalDateTime endTime
  //   ) {

  /**
   * Given no reservations
   * Then should return 0
   */
  /**
   * A ------
   * B         ------
   * Given no overlap with gap between the stations
   * Then should return 1
   */
  /**
   * A ------
   * B    ------
   * Given overlap between the stations
   * Then should return 2
   */
  /**
   * A ------
   * B       ------
   * Given no overlap no gap between the stations
   * Then should return 1
   */
  //   public int getMaximumChargersUsedAtSameTime(
  //     List<Reservation> overlappingReservations
  //   ) {

  /*
   * Given no reservations and maxCapacity of 2
   * Then should return true
   */
  /*
   * Given 2 overlapping at same time and maxCapacity of 2
   * Then should return false
   */
  /*
   * Given no overlapping at same time and maxCapacity of 2
   * Then should return true
   */
  //   public boolean hasAvailableSlot(
  //     List<Reservation> overlappingReservations,
  //     int maxCapacity
  //   ) {
}

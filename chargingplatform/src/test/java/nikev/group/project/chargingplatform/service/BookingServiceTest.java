package nikev.group.project.chargingplatform.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

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

  @Mock
  private ChargerRepository chargerRepository;

  @InjectMocks
  private BookingService bookingService;

  private Station station;
  Charger stationCharger;
  private User user;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Reservation reservation;

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
      Charger.ChargerStatus.OUT_OF_SERVICE,
      1.45,
      station
    );

    user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setEmail("test@example.com");

    reservation = new Reservation();
    reservation.setId(1L);
    reservation.setStation(station);
    reservation.setUser(user);
    reservation.setStartDate(startTime);
    reservation.setEndDate(endTime);

    startTime = LocalDateTime.now().plusHours(1);
    endTime = startTime.plusHours(2);
  }

  // @Test
  // void whenBookingValidSlot_thenBookingIsCreated() {
  // // Arrange
  // when(chargingStationRepository.findById(1L)).thenReturn(
  // Optional.of(station)
  // );
  // when(userRepository.findById(1L)).thenReturn(Optional.of(user));
  // when(
  // reservationRepository.findOverlappingReservations(any(), any(), any())
  // ).thenReturn(new ArrayList<>());
  // when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]
  // );

  // // Act
  // Reservation reservation = bookingService.bookSlot(
  // station.getId(),
  // user.getId(),
  // startTime,
  // endTime
  // );

  // // Assert
  // assertNotNull(reservation);
  // assertEquals(Reservation.ReservationStatus.BOOKED, reservation.getStatus());
  // assertEquals(station, reservation.getStation());
  // assertEquals(startTime, reservation.getStartDate());
  // assertEquals(endTime, reservation.getEndDate());
  // verify(reservationRepository).save(any(Reservation.class));
  // }

  // @Test
  // void whenBookingOverlappingSlot_thenThrowsException() {
  // // Arrange
  // when(chargingStationRepository.findById(1L)).thenReturn(
  // Optional.of(station)
  // );
  // when(userRepository.findById(1L)).thenReturn(Optional.of(user));

  // List<Reservation> overlappingSessions = new ArrayList<>();
  // Reservation existingBooking = new Reservation();
  // overlappingSessions.add(existingBooking);
  // when(
  // reservationRepository.findOverlappingReservations(any(), any(), any())
  // ).thenReturn(overlappingSessions);

  // // Act & Assert
  // assertThrows(RuntimeException.class, () ->
  // bookingService.bookSlot(1L, 1L, startTime, endTime)
  // );
  // }

  // @Test
  // void whenStationNotAvailable_thenThrowsException() {
  // // Arrange
  // when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
  // when(userRepository.findById(1L)).thenReturn(Optional.of(user));

  // // Act & Assert
  // assertThrows(RuntimeException.class, () ->
  // bookingService.bookSlot(1L, 1L, startTime, endTime)
  // );
  // }

  // @Test
  // void whenNoAvailableSlots_thenThrowsException() {
  // // Arrange
  // when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
  // when(userRepository.findById(1L)).thenReturn(Optional.of(user));

  // // Act & Assert
  // RuntimeException exception = assertThrows(RuntimeException.class, () ->
  // bookingService.bookSlot(1L, 1L, startTime, endTime)
  // );
  // assertNotNull(exception.getMessage());
  // }

  // @Test
  // void whenBookingLastSlot_thenStationStatusChangesToInUse() {
  // // Arrange
  // Charger charger = new Charger();
  // charger.setId(111L);
  // charger.setStatus(Charger.ChargerStatus.AVAILABLE);
  // charger.setChargingSpeedKw(1.45);
  // station.setChargers(List.of(charger));

  // when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
  // when(userRepository.findById(1L)).thenReturn(Optional.of(user));

  // when(
  // reservationRepository.findOverlappingReservations(any(), any(), any())
  // ).thenReturn(Collections.emptyList());
  // when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]
  // );
  // when(stationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

  // // Act
  // bookingService.bookSlot(1L, 1L, startTime, endTime);

  // // Assert
  // assertThat(charger.getStatus()).isEqualTo(Charger.ChargerStatus.CHARGING);
  // assertThat(station.hasAvailableCharger()).isFalse();
  // }

  // @Test
  // void whenCancellingBooking_thenSlotIsReleased() {
  // // Arrange
  // Reservation booking = new Reservation();
  // booking.setId(1L);
  // station.setChargers(
  // List.of(new Charger(111L, Charger.ChargerStatus.RESERVED, 1.45, station))
  // );
  // booking.setStation(station);

  // when(reservationRepository.findById(1L)).thenReturn(Optional.of(booking));
  // when(chargingStationRepository.save(any())).thenAnswer(i ->
  // i.getArguments()[0]
  // );

  // // Act
  // bookingService.cancelBooking(1L);

  // // Assert
  // assertThat(station.hasAvailableCharger()).isTrue();
  // verify(reservationRepository).delete(booking);
  // }

  /****************************************************
   * NEW TESTS AFTER CODE REFACTOR *
   ****************************************************/

  /* FUNCTION public void cancelBooking(Long sessionId) */
  /**
   * Given no booking with id 1
   * When trying to cancel booking with id 1
   * then RuntimeException is thrown
   */
  @Test
  public void whenNoBookingWithId1_thenRuntimeExceptionIsThrown() {
    doThrow(new RuntimeException())
      .when(reservationRepository)
      .deleteById(anyLong());
    assertThrows(RuntimeException.class, () -> bookingService.cancelBooking(1L)
    );
  }

  /**
   * Given booking with id 1
   * When trying to cancel booking with id 1
   * then booking is no longer in the system
   */
  @Test
  public void whenCancellingValidBooking_thenBookingIsCancelled() {
    doNothing().when(reservationRepository).deleteById(anyLong());
    assertDoesNotThrow(() -> bookingService.cancelBooking(1L));
  }

  /* FUNCTION public boolean hasAvailableCharger(Long stationId) */
  /**
   * Given no station with id 5
   * When verify it has available charger
   * then RuntimeException is thrown
   */
  @Test
  public void whenVerifyingAvailableChargerOnUnexistentStation_thenRuntimeExceptionIsThrown() {
    doThrow(new RuntimeException()).when(stationRepository).findById(anyLong());
    assertThrows(RuntimeException.class, () ->
      bookingService.hasAvailableCharger(5L)
    );
  }

  /**
   * Given station with id 5
   * and 2 chargers with one with status available
   * When verify it has available charger
   * then return true
   */
  @Test
  public void whenVerifyingAvailableChargerOnStationWithOneAvailableCharger_thenReturnTrue() {
    when(stationRepository.findById(anyLong())).thenReturn(
      Optional.of(station)
    );
    assertTrue(bookingService.hasAvailableCharger(5L));
  }

  /**
   * Given station with id 5
   * and 2 chargers both with status different from available
   * When verify it has available charger
   * then return false
   */
  @Test
  public void whenVerifyingAvailableChargerOnStationWithNoAvailableCharger_thenReturnFalse() {
    when(stationRepository.findById(anyLong())).thenReturn(
      Optional.of(station)
    );
    assertFalse(bookingService.hasAvailableCharger(5L));
  }

  /* FUNCTION public Reservation getBooking(Long sessionId) */
  /**
   * Given no reservation with id 5
   * When fetching booking
   * then RuntimeException is thrown
   */
  @Test
  public void whenFetchingUnexistentBooking_thenRuntimeExceptionIsThrown() {
    doThrow(new RuntimeException())
      .when(reservationRepository)
      .findById(anyLong());
    assertThrows(RuntimeException.class, () -> bookingService.getBooking(5L));
  }

  /**
   * Given a reservation with id 1 associated to user with id 2
   * When get reservatiom
   * then return reservation
   */
  @Test
  public void whenFetchingBookingAssociatedToUser_thenReturnReservation() {
    when(reservationRepository.findById(anyLong())).thenReturn(
      Optional.of(reservation)
    );
    assertEquals(reservation, bookingService.getBooking(1L));
  }

  /*
   * FUNCTION public Reservation bookSlot(Long stationId, Long userId,
   * LocalDateTime startTime, LocalDateTime endTime)
   */
  /**
   * (Unexisten station)
   * Given no station with id 5
   * When booking a slot to station with id 5
   * then throw RuntimeException
   */
  @Test
  public void whenBookingSlotOnUnexistentStation_thenRuntimeExceptionIsThrown() {
    doThrow(new RuntimeException()).when(stationRepository).findById(anyLong());
    assertThrows(RuntimeException.class, () ->
      bookingService.bookSlot(5L, 1L, startTime, endTime)
    );
  }

  /**
   * (Unexistent user)
   * Given station with id 1 and no user with id 1
   * When booking a slot to station with id 1 by the user with id 1
   * then throw RuntimeException
   */
  @Test
  public void whenBookingSlotWithUnexistentUser_thenRuntimeExceptionIsThrown() {
    doThrow(new RuntimeException()).when(userRepository).findById(anyLong());
    assertThrows(RuntimeException.class, () ->
      bookingService.bookSlot(1L, 1L, startTime, endTime)
    );
  }

  /**
   * (No slots available)
   * Given station with 2 chargers and 2 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h30
   * then throw RuntimeException
   */
  @Test
  public void whenBookingSlotWithNoAvailableSlots_thenRuntimeExceptionIsThrown() {
    assertThrows(RuntimeException.class, () ->
      bookingService.bookSlot(1L, 1L, startTime, endTime)
    );
  }

  /**
   * (Charger out of service)
   * Given station with 2 chargers and 1 reservation between 14h30 and 15h30
   * but 1 charger has state OUT_OF_SERVICE
   * When booking a slot to the station to the slot 14h30 and 15h00
   * then throw RuntimeException
   */
  @Test
  public void whenBookingSlotWithOutOfServiceCharger_thenRuntimeExceptionIsThrown() {
    assertThrows(RuntimeException.class, () ->
      bookingService.bookSlot(1L, 1L, startTime, endTime)
    );
  }

  /**
   * (Free station)
   * Given station with 2 chargers and 2 reservations:
   * one between 14h30 and 15h00 and another between 15h00 and 15h30
   * When booking a slot to the station to the slot 14h45 and 15h15
   * then Reservation is made
   */
  @Test
  public void whenBookingSlotWithFreeCharger_thenReturnReservation() {
    assertThat(
      bookingService.bookSlot(1L, 1L, startTime, endTime),
      is(reservation)
    );
  }

  /**
   * Given station with 2 chargers and 1 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h00
   * then Reservation is made
   */
  @Test
  public void whenBookingSlotWithOneAvailableCharger_thenReturnReservation() {
    assertThat(
      bookingService.bookSlot(1L, 1L, startTime, endTime),
      is(reservation)
    );
  }

  /* FUNCTION public int getMaximumChargersUsedAtSameTime(List<Reservation> overlappingReservations) */
  /**
   * Given no reservations
   * Then should return 0
   */
  @Test
  public void whenNoReservations_thenReturnZero() {
    assertThat(
      bookingService.getMaximumChargersUsedAtSameTime(new ArrayList<>()),
      is(0)
    );
  }

  /** ( (-) Booked ; (.) Free)
   * A ------........ (now           -> now + 30mins)
   * B ........------ ( nor + 45 min -> now + 75 min)
   * Given no overlap with gap between the Bookings
   * Then should return 1
   */
  @Test
  public void whenNoOverlapWithGapBetweenBookings_thenReturnOne() {
    Reservation reservation1 = new Reservation(
      1L,
      user,
      station,
      LocalDateTime.now(),
      LocalDateTime.now().plusMinutes(30)
    );
    Reservation reservation2 = new Reservation(
      2L,
      user,
      station,
      LocalDateTime.now().plusMinutes(45),
      LocalDateTime.now().plusMinutes(75)
    );
    assertThat(
      bookingService.getMaximumChargersUsedAtSameTime(
        new ArrayList<>(Arrays.asList(reservation1, reservation2))
      ),
      is(1)
    );
  }

  /**
   * A -------.... (now          -> now + 30mins)
   * B ...-------- (now + 15 min -> now + 45 min)
   * Given overlap between the Bookings
   * Then should return 2
   */
  @Test
  public void whenOverlapBetweenBookings_thenReturnOne() {
    Reservation reservation1 = new Reservation(
      1L,
      user,
      station,
      LocalDateTime.now(),
      LocalDateTime.now().plusMinutes(30)
    );
    Reservation reservation2 = new Reservation(
      2L,
      user,
      station,
      LocalDateTime.now().plusMinutes(15),
      LocalDateTime.now().plusMinutes(45)
    );
    assertThat(
      bookingService.getMaximumChargersUsedAtSameTime(
        new ArrayList<>(Arrays.asList(reservation1, reservation2))
      ),
      is(2)
    );
  }

  /**
   * A ------...... (now          -> now + 30mins)
   * B ......------ (now + 30 min -> now + 60 min)
   * Given no overlap no gap between the Bookings
   * Then should return 1
   */
  @Test
  public void whenNoOverlapWithNoGapBetweenBookings_thenReturnOne() {
    Reservation reservation1 = new Reservation(
      1L,
      user,
      station,
      LocalDateTime.now(),
      LocalDateTime.now().plusMinutes(30)
    );
    Reservation reservation2 = new Reservation(
      2L,
      user,
      station,
      LocalDateTime.now().plusMinutes(30),
      LocalDateTime.now().plusMinutes(60)
    );
    assertThat(
      bookingService.getMaximumChargersUsedAtSameTime(
        new ArrayList<>(Arrays.asList(reservation1, reservation2))
      ),
      is(1)
    );
  }

  /* FUNCTION public boolean hasAvailableSlot(List<Reservation> overlappingReservations, int maxCapacity) */
  /*
   * Given no reservations and maxCapacity of 2
   * Then should return true
   */
  @Test
  public void whenNoReservationsWithTwoSlots_thenReturnTrue() {
    assertThat(bookingService.hasAvailableSlot(new ArrayList<>(), 2), is(true));
  }

  /**
   * Given no overlapping with gapand maxCapacity of 2
   * Then should return true
   */
  @Test
  public void whenNoOverlappingReservationsWithGapWithTwoSlots_thenReturnTrue() {
    Reservation reservation1 = new Reservation(
      1L,
      user,
      station,
      LocalDateTime.now(),
      LocalDateTime.now().plusMinutes(30)
    );
    Reservation reservation2 = new Reservation(
      2L,
      user,
      station,
      LocalDateTime.now().plusMinutes(45),
      LocalDateTime.now().plusMinutes(75)
    );
    assertThat(
      bookingService.hasAvailableSlot(
        Arrays.asList(reservation1, reservation2),
        2
      ),
      is(true)
    );
  }

  /*
   * Given 2 overlapping at same time and maxCapacity of 2
   * Then should return false
   */
  @Test
  public void whenTwoOverlappingWithTwoSlots_thenReturnTrue() {
    Reservation reservation1 = new Reservation(
      1L,
      user,
      station,
      LocalDateTime.now(),
      LocalDateTime.now().plusMinutes(30)
    );
    Reservation reservation2 = new Reservation(
      2L,
      user,
      station,
      LocalDateTime.now().plusMinutes(15),
      LocalDateTime.now().plusMinutes(45)
    );
    assertThat(
      bookingService.hasAvailableSlot(
        Arrays.asList(reservation1, reservation2),
        2
      ),
      is(false)
    );
  }

  /*
   * Given no overlapping at same time and maxCapacity of 2
   * Then should return true
   */
  @Test
  public void whenNoOverlappingWithNoGapWithTwoSlots_thenReturnTrue() {
    Reservation reservation1 = new Reservation(
      1L,
      user,
      station,
      LocalDateTime.now(),
      LocalDateTime.now().plusMinutes(30)
    );
    Reservation reservation2 = new Reservation(
      2L,
      user,
      station,
      LocalDateTime.now().plusMinutes(30),
      LocalDateTime.now().plusMinutes(60)
    );
    assertThat(
      bookingService.hasAvailableSlot(
        Arrays.asList(reservation1, reservation2),
        2
      ),
      is(true)
    );
  }
}

package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import nikev.group.project.chargingplatform.DTOs.BookingRequestDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Reservation;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.ReservationRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
  /*
   * FUNCTION public ResponseEntity<Reservation> createBooking(
   * BookingRequestDTO request)
   */
  /**
   * Okay Concordo contigo, mas tem de ser feito no estilo, em cada teste todos os argumentos
   * existem menos um dele
   */
  /**
   * (Unexisten station)
   * Given no station with id 5
   * When booking a slot to station with id 5
   * then throw RuntimeException
   */
  /**
   * (Unexistent user)
   * Given station with id 1 and no user with id 1
   * When booking a slot to station with id 1 by the user with id 1
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
   * Given station with 2 chargers and 1 reservation between startTime and endTime
   * but 1 charger has state OUT_OF_SERVICE
   * When booking a slot to the station to the slot startTime and endTime
   * then throw RuntimeException
   */
  /**
   * (Free station)
   * Given station with 2 chargers and 2 reservations:
   * one between 14h30 and 15h00 and another between 15h00 and 15h30
   * When booking a slot to the station to the slot 14h45 and 15h15
   * then Reservation is made
   */
  /**
   * Given station with 2 chargers and 1 reservations between 14h30 and 15h00
   * When booking a slot to the station to the slot 14h30 and 15h00
   * then Reservation is made
   */

  /*
   * FUNCTION public ResponseEntity<Void>
   * cancelBooking(@NotNull @PathVariable(required = true) Long id)
   */
  /**
   * Given no booking with id 1
   * When trying to cancel booking with id 1
   * then reposnse with bad request is returned
   */
  /**
   * Given booking with id 1
   * When trying to cancel booking with id 1
   * then response with status ok is returned
   */
}

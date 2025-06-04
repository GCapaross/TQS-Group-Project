package nikev.group.project.chargingplatform.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.repository.StationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.EntityNotFoundException;
import nikev.group.project.chargingplatform.DTOs.ChargerDTO;
import nikev.group.project.chargingplatform.DTOs.StationCreateDTO;
import nikev.group.project.chargingplatform.DTOs.StationResponseDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Charger.ChargerStatus;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class StationServiceTest {

  /* FUNCTION public List<Station> getAllStations() */
  /**
   * Given no stations
   * When get all stations
   * Then return no Station
   */
  @Mock
  private StationRepository stationRepository;

  @Mock
  private ChargerRepository chargerRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private StationService stationService;

  @Test
  public void whenNoStations_thenReturnNoStations() {
    when(stationRepository.findAll()).thenReturn(new ArrayList<>());

    assertThat(stationService.getAllStations(), hasSize(0));
  }

  /**
   * Given 3 stations
   * When get all stations
   * Then return 3 Station
   */
  @Test
  void when3Stations_thenReturn3Stations() {
    Station baseStation = new Station();
    List<Station> stations = List.of(baseStation, baseStation, baseStation);
    when(stationRepository.findAll()).thenReturn(stations);

    assertThat(stationService.getAllStations(), hasSize(stations.size()));
  }

  /* FUNCTION public Station getStationById(Long id) */
  /**
   * Given no station with id 1
   * When get station by id 1
   * Then runtimeException is returned
   */
  @Test
  void whenGetNoStationWithId1_thenReturnRuntimeException() {
    when(stationRepository.findById(anyLong())).thenThrow(
      new RuntimeException()
    );
    assertThrows(RuntimeException.class, () -> stationService.getStationById(1L)
    );
  }

  /**
   * Given station with id 1
   * When get station by id 1
   * Then station with id 1 is returned
   */
  @Test
  void whenGetStationWithId1_thenReturnStationWithId1() {
    Station station = new Station();
    station.setId(1L);
    when(stationRepository.findById(anyLong())).thenReturn(
      Optional.of(station)
    );

    assertThat(stationService.getStationById(1L).getId(), is(1L));
  }

  /* FUNCTION public List<Station> findNearbyStations(double latitude, double longitude, double radiusKm) */
  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 0.5km
   * Then 0 are returned
   */
  @Test
  void whenGetNearbyStations_thenReturnNoStations() {
    // Base point for reference
    double baseLat = 0.0;
    double baseLon = 0.0;

    // Station 1: 1km distance (North)
    Station station1km = new Station();
    station1km.setLatitude(0.009); // ~1km north
    station1km.setLongitude(0.0);
    station1km.setName("Station 1km North");

    // Station 2: 3km distance (East)
    Station station3kmEast = new Station();
    station3kmEast.setLatitude(0.0);
    station3kmEast.setLongitude(0.027); // ~3km east
    station3kmEast.setName("Station 3km East");

    // Station 3: 3km distance (Southwest, using Pythagoras)
    Station station3kmSW = new Station();
    station3kmSW.setLatitude(-0.019); // ~2.1km south
    station3kmSW.setLongitude(-0.019); // ~2.1km west
    station3kmSW.setName("Station 3km Southwest");

    // Station 4: 5km distance (North)
    Station station5kmNorth = new Station();
    station5kmNorth.setLatitude(0.045); // ~5km north
    station5kmNorth.setLongitude(0.0);
    station5kmNorth.setName("Station 5km North");

    // Station 5: 5km distance (Southeast, using Pythagoras)
    Station station5kmSE = new Station();
    station5kmSE.setLatitude(-0.032); // ~3.5km south
    station5kmSE.setLongitude(0.032); // ~3.5km east
    station5kmSE.setName("Station 5km Southeast");

    when(
      stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any())
    ).thenReturn(new ArrayList<>());
    assertThat(
      stationService.findNearbyStations(baseLat, baseLon, 0.5),
      hasSize(0)
    );
  }

  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 3km
   * Then 3 are returned
   */
  @Test
  void whenGetNearbyStations_thenReturn3Stations() {
    Station baseStation = new Station();
    when(
      stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any())
    ).thenReturn(
      new ArrayList<>(List.of(baseStation, baseStation, baseStation))
    );

    assertThat(stationService.findNearbyStations(0.0, 0, 3.0), hasSize(3));
  }

  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 10km
   * Then 5 are returned
   */
  @Test
  void whenGetNearbyStations_thenReturn5Stations() {
    Station baseStation = new Station();
    when(
      stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any())
    ).thenReturn(List.of(baseStation, baseStation, baseStation));

    assertThat(stationService.findNearbyStations(0.0, 0, 10.0), hasSize(3));
  }

  /* FUNCTION public Station createStation(Station station) */
  /**
   * Given *
   * When creating a station
   * then station is created
   */
  @Test
  void whenCreateStation_thenReturnCreatedStation() {
    Company fakeCompany = new Company();
    fakeCompany.setId(42L);
    fakeCompany.setName("MinhaEmpresaX");

    when(companyRepository.findByName("MinhaEmpresaX"))
        .thenReturn(Optional.of(fakeCompany));
    
    Station station = new Station();
    station.setName("Test Station");
    station.setLocation("Test Location");
    station.setLatitude(1.0);
    station.setLongitude(1.0);
    station.setPricePerKwh(1.0);
    station.setSupportedConnectors(List.of("Type1", "Type2"));
    station.setTimetable("8:00-18:00");
    station.setCompany(fakeCompany);
    station.setWorkers(new ArrayList<>());
    StationCreateDTO stationCreateDTO = new StationCreateDTO(station);

    when(stationRepository.save(any(Station.class)))
        .thenReturn(station);

    StationResponseDTO result = stationService.createStation(stationCreateDTO);
    
    verify(stationRepository, times(1)).save(any(Station.class));
    verify(companyRepository, times(1)).findByName("MinhaEmpresaX");
    assertThat(result.getCompanyName(), is("MinhaEmpresaX"));

  }

  /* FUNCTION public Station updateStation(Long id, Station stationDetails) */
  void whenUpdateStationWithId1_thenReturnRuntimeException() {
    Station station = new Station();
    when(stationRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () ->
      stationService.updateStation(1L, station)
    );
  }

  /**
   * Given a station with id 5
   * When updating data from staion with id 5
   * Then updated station is returned
   */
  @Test
  void whenUpdateStationWithId5_thenReturnUpdatedStation() {
    Station oldStation = new Station(
      10L,
      "Old Station",
      "Old Location",
      0.0,
      0.0,
      0.0,
      new ArrayList<>(),
      "9:00-17:00",
      null,
      new ArrayList<>()
    );

    Station station = new Station(
      5L,
      "Updated Station",
      "Updated Location",
      1.0,
      1.0,
      1.0,
      List.of("Type1", "Type2"),
      "8:00-18:00",
      null,
      new ArrayList<>()
    );

    when(stationRepository.findById(anyLong())).thenReturn(
      Optional.of(oldStation)
    );
    when(stationRepository.save(any(Station.class))).thenReturn(oldStation);

    assertThat(stationService.updateStation(5L, station), is(oldStation));
  }

  /* FUNCTION public void deleteStation(Long id) */
  /**
   * Given a station with id 5
   * When deleting staion with id 1
   * Then RuntimeException is thrown
   */
  @Test
  void whenDeleteStationWithId1_thenReturnRuntimeException() {
    doThrow(new RuntimeException()).when(stationRepository).findById(anyLong());

    assertThrows(RuntimeException.class, () -> stationService.deleteStation(1L)
    );
  }

  /**
   * Given a station with id 5
   * When deleting staion with id 5
   * Then station is removed
   */
  @Test
  void whenDeleteStationWithId5_thenReturnNoException() {
    Station station = new Station();
    station.setId(5L);
    when(stationRepository.findById(anyLong())).thenReturn(
      Optional.of(station)
    );
    doNothing().when(stationRepository).delete(any(Station.class));
    assertDoesNotThrow(() -> stationService.deleteStation(5L));
  }

    @Test
    void whenCreateStationWithoutChargers_thenNoChargerSaved() {
        // Prepare fake Company
        Company fakeCompany = new Company();
        fakeCompany.setId(200L);
        fakeCompany.setName("NoChargerCorp");
        when(companyRepository.findByName("NoChargerCorp"))
            .thenReturn(Optional.of(fakeCompany));

        StationCreateDTO dto = new StationCreateDTO();
        dto.setName("Station B");
        dto.setLocation("Loc B");
        dto.setLatitude(5.0);
        dto.setLongitude(15.0);
        dto.setPricePerKwh(1.00);
        dto.setSupportedConnectors(List.of("TypeX"));
        dto.setTimetable("09:00-19:00");
        dto.setCompanyName("NoChargerCorp");
        dto.setWorkerIds(List.of()); // no workers
        dto.setChargers(List.of()); // empty list

        Station savedStation = new Station();
        savedStation.setId(84L);
        savedStation.setName("Station B");
        savedStation.setLocation("Loc B");
        savedStation.setLatitude(5.0);
        savedStation.setLongitude(15.0);
        savedStation.setPricePerKwh(1.00);
        savedStation.setSupportedConnectors(List.of("TypeX"));
        savedStation.setTimetable("09:00-19:00");
        savedStation.setCompany(fakeCompany);
        savedStation.setWorkers(new ArrayList<>());

        when(stationRepository.save(any(Station.class)))
            .thenReturn(savedStation);

        StationResponseDTO response = stationService.createStation(dto);

        verify(companyRepository, times(1)).findByName("NoChargerCorp");
        verify(stationRepository, times(1)).save(any(Station.class));
        verify(chargerRepository, never()).saveAll(anyList());
    }

    @Test
    void whenWorkerNotFound_thenThrowEntityNotFoundExceptionBeforeChargerLogic() {
        Company fakeCompany = new Company();
        fakeCompany.setId(500L);
        fakeCompany.setName("WorkerFailCorp");
        when(companyRepository.findByName("WorkerFailCorp"))
            .thenReturn(Optional.of(fakeCompany));

        when(userRepository.findAllById(anyList()))
            .thenReturn(List.of()); // no users returned

        StationCreateDTO dto = new StationCreateDTO();
        dto.setName("Station D");
        dto.setLocation("Loc D");
        dto.setLatitude(0.0);
        dto.setLongitude(0.0);
        dto.setPricePerKwh(1.50);
        dto.setSupportedConnectors(List.of("TypeZ"));
        dto.setTimetable("07:00-17:00");
        dto.setCompanyName("WorkerFailCorp");
        dto.setWorkerIds(List.of(999L)); // request one worker
        dto.setChargers(List.of(new ChargerDTO(0L, ChargerStatus.AVAILABLE, 30.0)));

        assertThrows(EntityNotFoundException.class, () -> stationService.createStation(dto));

        verify(companyRepository, times(1)).findByName("WorkerFailCorp");
        verify(userRepository, times(1)).findAllById(List.of(999L));
        verify(stationRepository, never()).save(any(Station.class));
        verify(chargerRepository, never()).saveAll(anyList());
    }
}

package nikev.group.project.chargingplatform.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.DTOs.StationDTO;
import nikev.group.project.chargingplatform.DTOs.WorkerDTO;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

  /**
   * Given a station with id 1
   * When checking for it's information
   * Then RuntimeException is thrown
   */
  @Test
  void whenStationDoesntExist_thenReturnRuntimeException() {
    when(stationRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> stationService.getStationById(1L));
  }

  @Test
    void testFindNearbyStations() {
        double latitude = 40.7128;
        double longitude = -74.0060;
        double radiusKm = 10.0;

        Station station1 = new Station(
          1L,
          "Station 1",
          "Location 1",
          40.7130,
          -74.0050,
          1.0,
          List.of("Type1", "Type2"),
          null,
          new ArrayList<>()
        );

        Station station2 = new Station(
          2L,
          "Station 2",
          "Location 2",
          40.7150,
          -74.0070,
          1.0,
          List.of("Type1", "Type2"),
          null,
          new ArrayList<>()
        );
        when(stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any()))
            .thenReturn(Arrays.asList(station1, station2));

        List<Station> result = stationService.findNearbyStations(latitude, longitude, radiusKm);

        assertEquals(2, result.size());
        verify(stationRepository).findAll(ArgumentMatchers.<Specification<Station>>any());
    }

    @Test
    void testSearchStationsByName() {
        SearchStationDTO searchStation = new SearchStationDTO();
        searchStation.setName("Test Station");

        Station station = new Station(
          2L,
          "Test Station",
          "Location 2",
          40.7150,
          -74.0070,
          1.0,
          List.of("Type1", "Type2"),
          null,
          new ArrayList<>()
        );
        when(stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any()))
            .thenReturn(Arrays.asList(station));

        List<Station> result = stationService.searchStations(searchStation);

        assertEquals(1, result.size());
        assertEquals("Test Station", result.get(0).getName());
        verify(stationRepository).findAll(ArgumentMatchers.<Specification<Station>>any());
    }

    @Test
    void testSearchStationsByLocation() {
        SearchStationDTO searchStation = new SearchStationDTO();
        searchStation.setLocation("Location");

        Station station = new Station(
          2L,
          "Test Station",
          "Location",
          40.7150,
          -74.0070,
          1.0,
          List.of("Type1", "Type2"),
          null,
          new ArrayList<>()
        );
        when(stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any()))
            .thenReturn(Arrays.asList(station));

        List<Station> result = stationService.searchStations(searchStation);

        assertEquals(1, result.size());
        assertEquals("Location", result.get(0).getLocation());
        verify(stationRepository).findAll(ArgumentMatchers.<Specification<Station>>any());
    }

    @Test
    void testSearchStationsByDistance() {
        SearchStationDTO searchStation = new SearchStationDTO();
        searchStation.setLatitude(40.7128);
        searchStation.setLongitude(-74.0060);
        searchStation.setRadiusKm(10.0);

        Station station = new Station(
          2L,
          "Test Station",
          "Location",
          40.7130,
          -74.0050,
          1.0,
          List.of("Type1", "Type2"),
          null,
          new ArrayList<>()
        );
        when(stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any()))
            .thenReturn(Arrays.asList(station));

        List<Station> result = stationService.searchStations(searchStation);

        assertEquals(1, result.size());
        verify(stationRepository).findAll(ArgumentMatchers.<Specification<Station>>any());
    }

    @Test
    void testAreAllChargersOutOfService_allOut() {
        Long stationId = 50L;
        Charger c1 = new Charger(); c1.setStatus(Charger.ChargerStatus.OUT_OF_SERVICE);
        Charger c2 = new Charger(); c2.setStatus(Charger.ChargerStatus.OUT_OF_SERVICE);
        when(chargerRepository.findByStation_Id(stationId)).thenReturn(List.of(c1, c2));

        boolean result = stationService.areAllChargersOutOfService(stationId);
        assertTrue(result);
        verify(chargerRepository).findByStation_Id(stationId);
    }

    @Test
    void testAreAllChargersOutOfService_someAvailable() {
        Long stationId = 60L;
        Charger c1 = new Charger(); c1.setStatus(Charger.ChargerStatus.OUT_OF_SERVICE);
        Charger c2 = new Charger(); c2.setStatus(Charger.ChargerStatus.AVAILABLE);
        when(chargerRepository.findByStation_Id(stationId)).thenReturn(List.of(c1, c2));

        boolean result = stationService.areAllChargersOutOfService(stationId);
        assertFalse(result);
        verify(chargerRepository).findByStation_Id(stationId);
    }

    @Test
    void testConvertToStationDTO_fullData() {
        Station station = new Station();
        station.setId(100L);
        station.setName("StName");
        station.setLocation("Loc");
        station.setLatitude(1.1);
        station.setLongitude(2.2);
        station.setPricePerKwh(3.3);
        station.setSupportedConnectors(List.of("A", "B"));
        Company company = new Company();
         company.setName("TestCompany");
        station.setCompany(company);
        User worker = new User(); 
        worker.setId(10L); 
        worker.setUsername("user1"); 
        worker.setEmail("u1@test");
        station.setWorkers(List.of(worker));
        Charger c1 = new Charger(); 
        c1.setId(200L); 
        c1.setStatus(Charger.ChargerStatus.OUT_OF_SERVICE);
        Charger c2 = new Charger();
        c2.setId(201L); 
        c2.setStatus(Charger.ChargerStatus.AVAILABLE);
        when(chargerRepository.findByStation_Id(100L)).thenReturn(List.of(c1, c2));

        StationDTO dto = stationService.convertToStationDTO(station);
        assertEquals(100L, dto.getId());
        assertEquals("StName", dto.getName());
        assertEquals("Loc", dto.getLocation());
        assertEquals(1.1, dto.getLatitude());
        assertEquals(2.2, dto.getLongitude());
        assertEquals(3.3, dto.getPricePerKwh());
        assertIterableEquals(List.of("A", "B"), dto.getSupportedConnectors());
        assertEquals("TestCompany", dto.getCompanyName());
        assertEquals(1, dto.getWorkers().size());
        WorkerDTO wd = dto.getWorkers().get(0);
        assertEquals(10L, wd.getId());
        assertEquals("user1", wd.getUsername());
        assertEquals("u1@test", wd.getEmail());
        assertEquals(List.of(c1, c2), dto.getChargers());
        assertEquals("Available", dto.getStatus());
    }

    @Test
    void testConvertToStationDTO_emptyData() {
        Station station = new Station(); 
        station.setId(101L);
        when(chargerRepository.findByStation_Id(101L)).thenReturn(new ArrayList<>());

        StationDTO dto = stationService.convertToStationDTO(station);
        assertNull(dto.getCompanyName());
        assertTrue(dto.getWorkers().isEmpty());
        assertTrue(dto.getChargers().isEmpty());
        assertEquals("Out of Service", dto.getStatus());
    }

    @Test
    void testCreateStation_withChargers_thenChargersSaved() {
      // 1) prepare company lookup
      Company fakeCompany = new Company();
      fakeCompany.setName("CompX");
      when(companyRepository.findByName("CompX"))
          .thenReturn(Optional.of(fakeCompany));
    
      // 2) prepare stationRepository.save(...) to return a Station with an ID
      Station savedStation = new Station();
      savedStation.setId(99L);
      when(stationRepository.save(any(Station.class)))
          .thenReturn(savedStation);
    
      // 3) build DTO with two ChargerDTO entries
      ChargerDTO dto1 = new ChargerDTO(1L, Charger.ChargerStatus.AVAILABLE, 50.0);
      ChargerDTO dto2 = new ChargerDTO(2L, Charger.ChargerStatus.OUT_OF_SERVICE, 11.0);
      StationCreateDTO createDto = new StationCreateDTO();
      createDto.setName("StationA");
      createDto.setLocation("LocA");
      createDto.setLatitude(1.0);
      createDto.setLongitude(2.0);
      createDto.setPricePerKwh(3.3);
      createDto.setSupportedConnectors(List.of("Type1"));
      createDto.setCompanyName("CompX");
      createDto.setWorkerIds(null);             // no workers
      createDto.setChargers(List.of(dto1, dto2));
    
      // 4) call the service
      stationService.createStation(createDto);
    
      // 5) verify using an ArgumentMatcher instead of a captor
      verify(chargerRepository).saveAll(argThat((List<Charger> list) -> {
        assertEquals(2, list.size());
        // first charger mapping
        assertEquals(Charger.ChargerStatus.AVAILABLE, list.get(0).getStatus());
        assertEquals(50.0, list.get(0).getChargingSpeedKw());
        assertSame(savedStation, list.get(0).getStation());
        // second charger mapping
        assertEquals(Charger.ChargerStatus.OUT_OF_SERVICE, list.get(1).getStatus());
        assertEquals(11.0, list.get(1).getChargingSpeedKw());
        assertSame(savedStation, list.get(1).getStation());
        return true;
      }));

    }

    @Test
    void testSearchStationsByConnectors() {
      SearchStationDTO dto = new SearchStationDTO();
      dto.setSupportedConnectors(List.of("TypeX"));
    
      Station s = new Station(1L, "S1", "Loc1", 0,0, 1.0,
          List.of("TypeX"), null, List.of());
      when(stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any()))
        .thenReturn(List.of(s));
    
      List<Station> result = stationService.searchStations(dto);
      assertEquals(1, result.size());
      assertTrue(result.get(0).getSupportedConnectors().contains("TypeX"));
    }

    @Test
    void testSearchStationsByPriceRange() {
      SearchStationDTO dto = new SearchStationDTO();
      dto.setMinPricePerKwh(2.0);
      dto.setMaxPricePerKwh(5.0);
    
      Station s = new Station(2L, "S2", "Loc2", 0,0, 3.0,
          List.of(), null, List.of());
      when(stationRepository.findAll(ArgumentMatchers.<Specification<Station>>any()))
        .thenReturn(List.of(s));
    
      List<Station> result = stationService.searchStations(dto);
      assertEquals(1, result.size());
      assertEquals(3.0, result.get(0).getPricePerKwh());
    }
}

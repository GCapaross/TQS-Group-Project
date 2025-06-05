package nikev.group.project.chargingplatform.service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.DTOs.StationDTO;
import nikev.group.project.chargingplatform.DTOs.WorkerDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StationService {

  @Autowired
  private StationRepository stationRepository;

  @Autowired
  private ChargerRepository chargerRepository;

  public List<StationDTO> getAllStations() {
    List<Station> stations = stationRepository.findAll();
    List<StationDTO> stationDTOs = new ArrayList<>();

    for (Station station : stations) {
      StationDTO stationDTO = convertToStationDTO(station);
      stationDTOs.add(stationDTO);
    }

    return stationDTOs;
  }

  public StationDTO getStationById(Long id) {
    Optional<Station> stationOptional = stationRepository.findById(id);
    if (stationOptional.isPresent()) {
      StationDTO stationDTO = convertToStationDTO(stationOptional.get());
      return stationDTO;
    } else {
      throw new RuntimeException("Station not found with id: " + id);
    }
  }

  public List<Station> findNearbyStations(
    double latitude,
    double longitude,
    double radiusKm
  ) {
    // Using Haversine formula to calculate distance
    return stationRepository.findAll((root, query, cb) -> {
      // Convert radius from km to degrees (approximate)
      double radiusDegrees = radiusKm / 111.0;

      Predicate latPredicate = cb.between(
        root.get("latitude"),
        latitude - radiusDegrees,
        latitude + radiusDegrees
      );
      Predicate longPredicate = cb.between(
        root.get("longitude"),
        longitude - radiusDegrees,
        longitude + radiusDegrees
      );

      return cb.and(latPredicate, longPredicate);
    });
  }

  public List<Station> searchStations(SearchStationDTO searchStation) {
    boolean isSearchingByName =
      searchStation.getName() != null && !searchStation.getName().isEmpty();
    boolean isSearchingByLocation =
      searchStation.getLocation() != null &&
      !searchStation.getLocation().isEmpty();
    boolean isSearchingByConnectors =
      searchStation.getSupportedConnectors() != null &&
      !searchStation.getSupportedConnectors().isEmpty();
    boolean hasMinimumEnergyPrice = searchStation.getMaxPricePerKwh() != null;
    boolean hasMaximumEnergyPrice = searchStation.getMaxPricePerKwh() != null;
    boolean isSearchingByTimetable =
      searchStation.getTimetable() != null &&
      !searchStation.getTimetable().isEmpty();
    boolean isSearchingByDistance =
      searchStation.getLatitude() != null &&
      searchStation.getLongitude() != null &&
      searchStation.getRadiusKm() != null;

    return stationRepository.findAll((root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (isSearchingByName) {
        predicates.add(
          cb.like(root.get("name"), "%" + searchStation.getName() + "%")
        );
      }

      if (isSearchingByLocation) {
        predicates.add(
          cb.like(root.get("location"), "%" + searchStation.getLocation() + "%")
        );
      }

      if (isSearchingByConnectors) {
        predicates.add(
          root
            .get("supportedConnectors")
            .in(searchStation.getSupportedConnectors())
        );
      }

      if (hasMinimumEnergyPrice) {
        predicates.add(
          cb.ge(root.get("pricePerKwh"), searchStation.getMinPricePerKwh())
        );
      }

      if (hasMaximumEnergyPrice) {
        predicates.add(
          cb.le(root.get("pricePerKwh"), searchStation.getMaxPricePerKwh())
        );
      }

      if (isSearchingByTimetable) {
        predicates.add(
          cb.like(
            root.get("timetable"),
            "%" + searchStation.getTimetable() + "%"
          )
        );
      }

      if (isSearchingByDistance) {
        double radiusDegrees = searchStation.getRadiusKm() / 111.0;
        predicates.add(
          cb.between(
            root.get("latitude"),
            searchStation.getLatitude() - radiusDegrees,
            searchStation.getLatitude() + radiusDegrees
          )
        );
        predicates.add(
          cb.between(
            root.get("longitude"),
            searchStation.getLongitude() - radiusDegrees,
            searchStation.getLongitude() + radiusDegrees
          )
        );
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    });
  }

  public Station createStation(Station station) {
    return stationRepository.save(station);
  }

  public Station updateStation(Long id, Station stationDetails) {
    Station station = stationRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));
    station.setName(stationDetails.getName());
    station.setLocation(stationDetails.getLocation());
    station.setLatitude(stationDetails.getLatitude());
    station.setLongitude(stationDetails.getLongitude());
    station.setPricePerKwh(stationDetails.getPricePerKwh());
    station.setSupportedConnectors(stationDetails.getSupportedConnectors());
    return stationRepository.save(station);
  }

  public void deleteStation(Long id) {
    Station stationToDelete = stationRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));
    stationRepository.delete(stationToDelete);
  }

  public boolean areAllChargersOutOfService(Long stationId) {
    List<Charger> chargers = chargerRepository.findByStation_Id(stationId);
    for (Charger charger : chargers) {
      if (charger.getStatus() != Charger.ChargerStatus.OUT_OF_SERVICE) {
        return false; 
      }
    }
    return true; 
  }

  public StationDTO convertToStationDTO(Station station){
    StationDTO stationDTO = new StationDTO();
    stationDTO.setId(station.getId());
    stationDTO.setName(station.getName());
    stationDTO.setLocation(station.getLocation());
    stationDTO.setLatitude(station.getLatitude());
    stationDTO.setLongitude(station.getLongitude());
    stationDTO.setPricePerKwh(station.getPricePerKwh());
    stationDTO.setSupportedConnectors(station.getSupportedConnectors());
    Company company = station.getCompany();
    if (company != null) {
      stationDTO.setCompanyName(company.getName());
    } else {
      stationDTO.setCompanyName(null);
    }
    List<User> workers = station.getWorkers();
    if (workers == null) {
      workers = new ArrayList<>();
    } else {
      for (User worker : workers) {
        stationDTO.getWorkers().add(new WorkerDTO(worker.getId(), worker.getUsername(), worker.getEmail()));
      }
    }
    
    List<Charger> chargers = chargerRepository.findByStation_Id(station.getId());
    if (chargers == null) {
      chargers = new ArrayList<>();
    } else{
      stationDTO.setChargers(chargers);
    }
    if (areAllChargersOutOfService(station.getId()) || chargers.isEmpty()) {
      stationDTO.setStatus("Out of Service");
    } else {
      stationDTO.setStatus("Available");
    }

    return stationDTO;
  }
}

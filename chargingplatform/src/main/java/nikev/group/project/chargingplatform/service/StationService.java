package nikev.group.project.chargingplatform.service;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.repository.StationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import nikev.group.project.chargingplatform.DTOs.StationCreateDTO;
import nikev.group.project.chargingplatform.DTOs.StationResponseDTO;
import nikev.group.project.chargingplatform.DTOs.StationWithChargerSpeedsDTO;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;

@Service
public class StationService {

  @Autowired
  private StationRepository stationRepository;
  @Autowired
  private ChargerRepository chargerRepository;
  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private UserRepository userRepository;

  public List<StationWithChargerSpeedsDTO> getAllStations() {
    List<Station> stations = stationRepository.findAll();

    return stations.stream()
                .map(station -> {
                    // 2.1) Busca todos os chargers que têm station_id = station.getId()
                    List<Charger> chargers = chargerRepository.findByStation_Id(station.getId());

                    // 2.2) Extrai apenas o campo chargingSpeedKw
                    List<Double> speeds = chargers.stream()
                            .map(Charger::getChargingSpeedKw)
                            .collect(Collectors.toList());

                    return new StationWithChargerSpeedsDTO(
                            station.getId(),
                            station.getName(),
                            station.getLocation(),
                            station.getLatitude(),
                            station.getLongitude(),
                            station.getPricePerKwh(),
                            station.getSupportedConnectors(),
                            station.getTimetable(),
                            speeds
                    );
                })
                .collect(Collectors.toList());
  }

  public Station getStationById(Long id) {
    return stationRepository
      .findById(id)
      .orElseThrow(() ->
        new RuntimeException("Station not found with id: " + id)
      );
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

  public StationResponseDTO createStation(StationCreateDTO dto) {
    Company company = companyRepository
                .findByName(dto.getCompanyName())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Company '" + dto.getCompanyName() + "' not found"
                ));

    List<User> workers = List.of();
    if (dto.getWorkerIds() != null && !dto.getWorkerIds().isEmpty()) {
        workers = userRepository.findAllById(dto.getWorkerIds());
        if (workers.size() != dto.getWorkerIds().size()) {
            throw new EntityNotFoundException(
                "Some workers not found with provided IDs"
            );
        }
    }

    Station station = new Station();
    station.setName(dto.getName());
    station.setLocation(dto.getLocation());
    station.setLatitude(dto.getLatitude());
    station.setLongitude(dto.getLongitude());
    station.setPricePerKwh(dto.getPricePerKwh());
    station.setSupportedConnectors(dto.getSupportedConnectors());
    station.setTimetable(dto.getTimetable());
    if (company != null) {
        station.setCompany(company);
    }
    station.setWorkers(workers);

    Station saved = stationRepository.save(station);

    return new StationResponseDTO(saved);
  }

  public Station updateStation(Long id, Station stationDetails) {
    Station station = getStationById(id);
    station.setName(stationDetails.getName());
    station.setLocation(stationDetails.getLocation());
    station.setLatitude(stationDetails.getLatitude());
    station.setLongitude(stationDetails.getLongitude());
    station.setPricePerKwh(stationDetails.getPricePerKwh());
    station.setSupportedConnectors(stationDetails.getSupportedConnectors());
    station.setTimetable(stationDetails.getTimetable());
    return stationRepository.save(station);
  }

  public void deleteStation(Long id) {
    Station stationToDelete = getStationById(id);
    stationRepository.delete(stationToDelete);
  }
}

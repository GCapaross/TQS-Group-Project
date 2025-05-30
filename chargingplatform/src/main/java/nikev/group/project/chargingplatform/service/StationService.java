package nikev.group.project.chargingplatform.service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import nikev.group.project.chargingplatform.DTOs.SearchStationDTO;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StationService {

  @Autowired
  private StationRepository stationRepository;

  public List<Station> getAllStations() {
    return stationRepository.findAll();
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

  public Station createStation(Station station) {
    return stationRepository.save(station);
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

package nikev.group.project.chargingplatform.service;

import java.util.List;
import nikev.group.project.chargingplatform.model.Station;

public class StationServiceTest {
  /**
   * Given no stations
   * When get all stations
   * Then return no Station
   */
  /**
   * Given 10 stations
   * When get all stations
   * Then return 10 Station
   */
  // public List<Station> getAllStations()

  /**
   * Given station with id 5
   * When get station by id 1
   * Then runtimeException is returned
   */
  /**
   * Given station with id 5
   * When get station by id 5
   * Then station with id 5 is returned
   */
  // public Station getStationById(Long id) {

  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 0.5km
   * Then 0 are returned
   */
  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 3km
   * Then 3 are returned
   */
  /**
   * Given 5 stations and a lat and lon
   *  One at a distance of 1km of lat and lon
   *  Two at a distance of 3km of lat and lon
   *  Two at a distance of 5km of lat and lon
   * When fetching nearby stations of lat and lon of a radius of 10km
   * Then 5 are returned
   */
  // public List<Station> findNearbyStations(double latitude, double longitude, double radiusKm) {

  /**
   * Given *
   * When creating a station
   * then station is created
   */
  // public Station createStation(Station station) {

  /**
   * Given a station with id 5
   * When updating data from staion with id 1
   * Then RuntimeException is thrown
   */
  /**
   * Given a station with id 5
   * When updating data from staion with id 5
   * Then updated station is returned
   */
  // public Station updateStation(Long id, Station stationDetails) {

  /**
   * Given a station with id 5
   * When deleting staion with id 1
   * Then RuntimeException is thrown
   */
  /**
   * Given a station with id 5
   * When deleting staion with id 5
   * Then station is removed
   */
  // public void deleteStation(Long id) {
}

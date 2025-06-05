package nikev.group.project.chargingplatform.utils;

import nikev.group.project.chargingplatform.DTOs.StationBasicDTO;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;

public class Utils {
    public static void updateStationsDTOs(Station station, StationBasicDTO dto) {
        dto.setName(station.getName());
        dto.setLocation(station.getLocation());
        dto.setLatitude(station.getLatitude());
        dto.setLongitude(station.getLongitude());
        dto.setPricePerKwh(station.getPricePerKwh());
        dto.setSupportedConnectors(station.getSupportedConnectors());
        dto.setTimetable(station.getTimetable());
        if (station.getCompany() != null) {
            dto.setCompanyName(station.getCompany().getName());
        }
        if (station.getWorkers() != null && !station.getWorkers().isEmpty()) {
            dto.setWorkerIds(station.getWorkers()
                                    .stream()
                                    .map(User::getId)
                                    .toList());
        }
    }
}
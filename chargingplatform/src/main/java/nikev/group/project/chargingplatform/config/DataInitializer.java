package nikev.group.project.chargingplatform.config;

import nikev.group.project.chargingplatform.model.ChargingStation;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.ChargingStationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Override
    public void run(String... args) {
        // Create default admin user if it doesn't exist
        if (!userRepository.findByEmail("admin@admin.com").isPresent()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@admin.com");
            admin.setPassword("admin");
            userRepository.save(admin);
        }

        // Add charging stations in Portugal if they don't exist
        if (chargingStationRepository.count() == 0) {
            // Lisbon stations
            ChargingStation lisbonCentral = new ChargingStation();
            lisbonCentral.setName("Lisbon Central Charging Hub");
            lisbonCentral.setLocation("Avenida da Liberdade, Lisbon");
            lisbonCentral.setLatitude(38.7223);
            lisbonCentral.setLongitude(-9.1393);
            lisbonCentral.setStatus("AVAILABLE");
            lisbonCentral.setMaxSlots(8);
            lisbonCentral.setAvailableSlots(8);
            lisbonCentral.setPricePerKwh(0.35);
            lisbonCentral.setConnectorTypes(Arrays.asList("CCS", "Type 2", "CHAdeMO"));
            lisbonCentral.setChargingSpeedKw(150.0);
            lisbonCentral.setCarrierNetwork("EDP");
            lisbonCentral.setAverageRating(4.5);
            chargingStationRepository.save(lisbonCentral);

            // Porto stations
            ChargingStation portoStation = new ChargingStation();
            portoStation.setName("Porto Riverside Charging");
            portoStation.setLocation("Ribeira, Porto");
            portoStation.setLatitude(41.1408);
            portoStation.setLongitude(-8.6161);
            portoStation.setStatus("AVAILABLE");
            portoStation.setMaxSlots(6);
            portoStation.setAvailableSlots(6);
            portoStation.setPricePerKwh(0.32);
            portoStation.setConnectorTypes(Arrays.asList("CCS", "Type 2"));
            portoStation.setChargingSpeedKw(120.0);
            portoStation.setCarrierNetwork("Galp");
            portoStation.setAverageRating(4.3);
            chargingStationRepository.save(portoStation);

            // Faro stations
            ChargingStation faroStation = new ChargingStation();
            faroStation.setName("Faro Airport Charging");
            faroStation.setLocation("Faro International Airport");
            faroStation.setLatitude(37.0144);
            faroStation.setLongitude(-7.9659);
            faroStation.setStatus("AVAILABLE");
            faroStation.setMaxSlots(4);
            faroStation.setAvailableSlots(4);
            faroStation.setPricePerKwh(0.38);
            faroStation.setConnectorTypes(Arrays.asList("CCS", "Type 2", "CHAdeMO"));
            faroStation.setChargingSpeedKw(180.0);
            faroStation.setCarrierNetwork("Repsol");
            faroStation.setAverageRating(4.7);
            chargingStationRepository.save(faroStation);

            // Coimbra stations
            ChargingStation coimbraStation = new ChargingStation();
            coimbraStation.setName("Coimbra University Charging");
            coimbraStation.setLocation("University of Coimbra");
            coimbraStation.setLatitude(40.2089);
            coimbraStation.setLongitude(-8.4257);
            coimbraStation.setStatus("AVAILABLE");
            coimbraStation.setMaxSlots(4);
            coimbraStation.setAvailableSlots(4);
            coimbraStation.setPricePerKwh(0.30);
            coimbraStation.setConnectorTypes(Arrays.asList("CCS", "Type 2"));
            coimbraStation.setChargingSpeedKw(100.0);
            coimbraStation.setCarrierNetwork("EDP");
            coimbraStation.setAverageRating(4.2);
            chargingStationRepository.save(coimbraStation);
        }
    }
} 

// mvn spring-boot:run
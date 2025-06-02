package nikev.group.project.chargingplatform.config;

import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Charger.ChargerStatus;
import nikev.group.project.chargingplatform.repository.StationRepository;
import nikev.group.project.chargingplatform.repository.UserRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not present
        Optional<User> adminOpt = userRepository.findByEmail("admin@admin.com");
        if (!adminOpt.isPresent()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@admin.com");
            admin.setPassword("admin");
            userRepository.save(admin);
        }
        // Create default company if none
        User admin = userRepository.findByEmail("admin@admin.com").get();
        if (companyRepository.count() == 0) {
            Company defaultCompany = new Company();
            defaultCompany.setName("Default Company");
            defaultCompany.setOwner(admin);
            companyRepository.save(defaultCompany);
        }

        // Initialize stations if none exist
        if (stationRepository.count() == 0) {
            List<Station> stations = new ArrayList<>();

            Station lisbon = new Station();
            lisbon.setName("Lisbon Central Charging Hub");
            lisbon.setLocation("Avenida da Liberdade, Lisbon");
            lisbon.setLatitude(38.7223);
            lisbon.setLongitude(-9.1393);
            lisbon.setPricePerKwh(0.35);
            lisbon.setSupportedConnectors(Arrays.asList("CCS", "Type 2", "CHAdeMO"));
            lisbon.setTimetable("24/7");
            stations.add(lisbon);

            Station porto = new Station();
            porto.setName("Porto Riverside Charging");
            porto.setLocation("Ribeira, Porto");
            porto.setLatitude(41.1408);
            porto.setLongitude(-8.6161);
            porto.setPricePerKwh(0.32);
            porto.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
            porto.setTimetable("24/7");
            stations.add(porto);

            Station faro = new Station();
            faro.setName("Faro Airport Charging");
            faro.setLocation("Faro International Airport");
            faro.setLatitude(37.0144);
            faro.setLongitude(-7.9659);
            faro.setPricePerKwh(0.38);
            faro.setSupportedConnectors(Arrays.asList("CCS", "Type 2", "CHAdeMO"));
            faro.setTimetable("24/7");
            stations.add(faro);

            Station coimbra = new Station();
            coimbra.setName("Coimbra University Charging");
            coimbra.setLocation("University of Coimbra");
            coimbra.setLatitude(40.2089);
            coimbra.setLongitude(-8.4257);
            coimbra.setPricePerKwh(0.30);
            coimbra.setSupportedConnectors(Arrays.asList("CCS", "Type 2"));
            coimbra.setTimetable("24/7");
            stations.add(coimbra);

            stationRepository.saveAll(stations);
            // Assign company to stations and save
            Company company = companyRepository.findAll().get(0);
            stations.forEach(s -> s.setCompany(company));
            stationRepository.saveAll(stations);
            // Initialize chargers for each station
            if (chargerRepository.count() == 0) {
                List<Charger> chargers = new ArrayList<>();
                for (Station s : stations) {
                    // create two chargers per station
                    for (int i = 0; i < 2; i++) {
                        Charger c = new Charger();
                        c.setStatus(ChargerStatus.AVAILABLE);
                        c.setChargingSpeedKw(50.0);
                        c.setStation(s);
                        chargers.add(c);
                    }
                }
                chargerRepository.saveAll(chargers);
            }
        }
    }
}
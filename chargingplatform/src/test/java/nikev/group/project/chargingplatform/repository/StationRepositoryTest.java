package nikev.group.project.chargingplatform.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import nikev.group.project.chargingplatform.model.Station;

@DataJpaTest
public class StationRepositoryTest {

    @Autowired
    private StationRepository stationRepository;

    @Test
    @DisplayName("Deve salvar e recuperar uma Station pelo ID")
    void saveAndFindById() {
        Station s = new Station();
        s.setName("Estacao Teste");
        s.setLocation("Lisboa");
        s.setLatitude(38.7);
        s.setLongitude(-9.1);
        s.setPricePerKwh(0.20);
        s.setSupportedConnectors(Arrays.asList("Type2", "CHAdeMO"));
        s.setTimetable("24h");

        Station saved = stationRepository.save(s);

        assertThat(saved.getId()).isNotNull();

        Optional<Station> maybe = stationRepository.findById(saved.getId());
        assertThat(maybe).isPresent();
        Station found = maybe.get();
        assertThat(found.getName()).isEqualTo("Estacao Teste");
        assertThat(found.getLocation()).isEqualTo("Lisboa");
        assertThat(found.getPricePerKwh()).isEqualTo(0.20);
        assertThat(found.getSupportedConnectors()).containsExactlyInAnyOrder("Type2", "CHAdeMO");
    }

    @Test
    @DisplayName("existsById deve retornar verdadeiro se a Station existir")
    void existsById_returnsTrue() {
        Station s = new Station();
        s.setName("Outra Estacao");
        s.setLocation("Porto");
        s.setLatitude(41.1);
        s.setLongitude(-8.6);
        s.setPricePerKwh(0.25);
        s.setSupportedConnectors(List.of("CCS"));
        s.setTimetable("08-20h");
        Station saved = stationRepository.save(s);

        boolean exists = stationRepository.existsById(saved.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("deleteById deve remover a Station corretamente")
    void deleteById_removesStation() {
        Station s = new Station();
        s.setName("Estacao Para Deletar");
        s.setLocation("Coimbra");
        s.setLatitude(40.2);
        s.setLongitude(-8.4);
        s.setPricePerKwh(0.18);
        s.setSupportedConnectors(List.of("Type1"));
        s.setTimetable("10-22h");
        Station saved = stationRepository.save(s);

        assertThat(stationRepository.existsById(saved.getId())).isTrue();

        stationRepository.deleteById(saved.getId());

        assertThat(stationRepository.existsById(saved.getId())).isFalse();
    }
}

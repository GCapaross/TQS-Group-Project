package nikev.group.project.chargingplatform.IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import nikev.group.project.chargingplatform.model.Charger;
import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.model.Station;
import nikev.group.project.chargingplatform.model.Charger.ChargerStatus;
import nikev.group.project.chargingplatform.repository.ChargerRepository;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import nikev.group.project.chargingplatform.repository.StationRepository;

public class StationControllerSteps {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    private ResponseEntity<String> response;
    private final ObjectMapper mapper = new ObjectMapper();
    private Long savedStationId; 

    @Given("a company named {string} exists")
    public void a_company_named_exists(String companyName) {
        // Ensure the company is present in the test database
        if (companyRepository.findByName(companyName).isEmpty()) {
            Company company = new Company();
            company.setName(companyName);
            companyRepository.save(company);
            assertThat(companyRepository.findByName(companyName)).isPresent();
        }
    }

    @And("a company named {string} already exists")
    public void a_company_named_already_exists(String companyName) {
        a_company_named_exists(companyName);
    }

    @And("a station exists for {string}")
    public void a_station_with_id_exists(String companyName) {
        Company c = companyRepository.findByName(companyName)
            .orElseThrow(() -> new IllegalStateException("Company not found"));

        Station s = new Station();
        s.setName("Test Station");
        s.setCompany(c);
        stationRepository.save(s);

        System.out.println("Station created with ID: " + s.getId());

        Charger charger = new Charger();
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStation(s);
        chargerRepository.save(charger);
    }

    @And("{int} stations exist for {string}")
    public void n_stations_exist_for_company(int count, String companyName) {
        Company company = companyRepository.findByName(companyName)
            .orElseThrow(() -> new IllegalStateException("Company not found: " + companyName));
        
        for (int i = 0; i < count; i++) {
            Station station = new Station();
            station.setName("Test Station " + (i + 1));
            station.setCompany(company);
            stationRepository.save(station);
        }
    }

    @When("I send a POST to {string} to create a station with body")
    public void i_send_post_create_station_with_body(String path, DataTable table) throws Exception {
        Map<String,String> map = table.asMap(String.class, String.class);  
        ObjectNode bodyNode = mapper.createObjectNode();

        for (var entry : map.entrySet()) {
            String key = entry.getKey().trim();
            String val = entry.getValue().trim();

            if (val == null || val.isBlank()) {
                continue;
            }
            // If it looks like a JSON array
            if (val.startsWith("[")) {
                JsonNode arrayNode = mapper.readTree(val);
                bodyNode.set(key, arrayNode);
            }
            // If it's numeric
            else if (val.matches("^-?\\d+(\\.\\d+)?$")) {
                if (val.contains(".")) {
                    bodyNode.put(key, Double.parseDouble(val));
                } else {
                    bodyNode.put(key, Integer.parseInt(val));
                }
            }
            // Otherwise
            else {
                bodyNode.put(key, val);
            }
        }

        String jsonBody = mapper.writeValueAsString(bodyNode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        response = rest.exchange(path, HttpMethod.POST, entity, String.class);
    }

    @When("I send a GET to {string} to retrieve all stations")
    public void i_send_get_to_retrieve_all_stations(String path) {
        if (path.contains("{id}")){
            path = path.replace("{id}", savedStationId.toString());
        }
        response = rest.exchange(path, HttpMethod.GET, HttpEntity.EMPTY, String.class);
    }

    @Then("the station creation response status should be {int}")
    public void the_response_status_after_station_creation_should_be(Integer expectedStatus) {
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("the station creation response should contain:")
    public void the_response_after_station_creation_should_contain(DataTable table) throws Exception {
        Map<String, String> map = table.asMap(String.class, String.class);
        String responseBody = response.getBody();
        JsonNode json = mapper.readTree(responseBody);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String expected = entry.getValue();
            JsonNode node = json.path(key);
            if (node.isMissingNode()) {
                throw new IllegalStateException("Key '" + key + "' not found in response");
            }
            
            if (expected.matches("^-?\\d+(\\.\\d+)?$")) {
                double exp = Double.parseDouble(expected);
                assertThat(node.asDouble()).isEqualTo(exp);
            }
            else {
                assertThat(node.asText()).isEqualTo(expected);
            }
        }
    }

    @And("I save the station id")
    public void i_save_the_station_id() throws Exception {
      JsonNode json = mapper.readTree(response.getBody());
      savedStationId = json.get("id").asLong();
    }
}

package nikev.group.project.chargingplatform;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.cucumber.spring.CucumberContextConfiguration;
import nikev.group.project.chargingplatform.TestcontainersConfiguration;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8080")
@Testcontainers
@ActiveProfiles("test") 
@Import({TestcontainersConfiguration.class, TestMetricConfig.class})
public class SpringCucumberConfiguration {

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }
}

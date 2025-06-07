package nikev.group.project.chargingplatform.FunctionalTests;

import io.cucumber.spring.CucumberContextConfiguration;
import nikev.group.project.chargingplatform.TestcontainersConfiguration;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8080")
@Testcontainers
@ActiveProfiles("test") 
@Import(TestcontainersConfiguration.class)
public class SpringCucumberConfiguration {

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }
}

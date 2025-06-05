package nikev.group.project.chargingplatform;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("nikev/group/project/chargingplatform")
@ConfigurationParameter(
  key = GLUE_PROPERTY_NAME,
  value = "nikev.group.project.chargingplatform.FunctionalTests"
)
@ConfigurationParameter(
   key = FILTER_TAGS_PROPERTY_NAME,
   value = "@SKIP_ALL"
)
public class CucumberTest {}

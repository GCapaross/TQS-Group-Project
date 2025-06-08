package nikev.group.project.chargingplatform;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nikev.group.project.chargingplatform.config.MetricsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class TestMetricConfig {

  @Bean
  public MeterRegistry MeterRegistry() {
    return new SimpleMeterRegistry();
  }
}

package nikev.group.project.chargingplatform.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter requestCounter(MeterRegistry registry) {
        return Counter.builder("app.requests.total")
                .description("Total number of requests")
                .register(registry);
    }

    @Bean
    public Timer requestTimer(MeterRegistry registry) {
        return Timer.builder("app.requests.latency")
                .description("Request latency")
                .register(registry);
    }
} 
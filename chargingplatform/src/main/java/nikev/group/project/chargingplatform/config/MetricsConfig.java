package nikev.group.project.chargingplatform.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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

  @Bean
  public Counter bookingSuccessCounter(MeterRegistry registry) {
    return Counter.builder("app.bookings.success")
      .description("Number of successful bookings")
      .register(registry);
  }

  @Bean
  public Counter bookingFailureCounter(MeterRegistry registry) {
    return Counter.builder("app.bookings.failure")
      .description("Number of failed bookings")
      .register(registry);
  }

  @Bean
  public MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }

  @Bean
  public Timer bookingDurationTimer(MeterRegistry registry) {
    return Timer.builder("app.bookings.duration")
      .description("Time taken to process bookings")
      .register(registry);
  }

  @Bean
  public Counter cancellationSuccessCounter(MeterRegistry registry) {
    return Counter.builder("app.bookings.cancellation.success")
      .description("Number of successful cancellations")
      .register(registry);
  }

  @Bean
  public Counter cancellationFailureCounter(MeterRegistry registry) {
    return Counter.builder("app.bookings.cancellation.failure")
      .description("Number of failed cancellations")
      .register(registry);
  }

  @Bean
  public ClassLoaderMetrics classLoaderMetrics() {
    return new ClassLoaderMetrics();
  }

  @Bean
  public JvmMemoryMetrics jvmMemoryMetrics() {
    return new JvmMemoryMetrics();
  }

  @Bean
  public JvmGcMetrics jvmGcMetrics() {
    return new JvmGcMetrics();
  }

  @Bean
  public JvmThreadMetrics jvmThreadMetrics() {
    return new JvmThreadMetrics();
  }

  @Bean
  public ProcessorMetrics processorMetrics() {
    return new ProcessorMetrics();
  }

  @Bean
  public UptimeMetrics uptimeMetrics() {
    return new UptimeMetrics();
  }
}

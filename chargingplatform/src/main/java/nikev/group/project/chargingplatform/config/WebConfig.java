package nikev.group.project.chargingplatform.config;

import java.util.List;
import nikev.group.project.chargingplatform.security.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${frontend.host}")
    private String hostname;

    @Value("${frontend.port}")
    private String frontendDockerPort;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
            List.of(
                "http://" + hostname + ":" + frontendDockerPort,
                "http://" + hostname + ":" + frontendDockerPort + "/",
                "http://" + hostname,
                "http://" + hostname + "/"
            )
        );
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http)
        throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/users/login",
                        "/api/users/register"
                    )
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**"
                    )
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/bookings")
                    .authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/bookings/{id}")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/users/me")
                    .authenticated()
                    .anyRequest()
                    .permitAll()
            )
            .addFilterBefore(
                jwtTokenFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }
}

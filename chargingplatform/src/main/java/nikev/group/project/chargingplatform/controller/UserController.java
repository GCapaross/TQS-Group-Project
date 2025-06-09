package nikev.group.project.chargingplatform.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import nikev.group.project.chargingplatform.DTOs.LoginRequestDTO;
import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.security.JwtTokenProvider;
import nikev.group.project.chargingplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String APPLICATION_NAME = "chargingplatform";
    private static final String APPLICATION_TAG = "application";
    private static final String REGISTRATION_LATENCY = "app_registration_latency";
    private static final String LOGIN_LATENCY = "app_login_latency";
    private static final String STATUS_TAG = "status";
    private static final String STATUS_FAILURE = "failure";
    private static final String STATUS_SUCCESS = "success";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final MeterRegistry meterRegistry;
    private final Counter userRegistrationCounter;
    private final Counter loginCounter;

    public UserController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userRegistrationCounter = Counter.builder(
            "app_users_registered_total"
        )
            .description("Total number of user registrations")
            .tag(APPLICATION_TAG, APPLICATION_NAME)
            .register(meterRegistry);
        this.loginCounter = Counter.builder("app_logins_total")
            .description("Total number of login attempts")
            .tag(APPLICATION_TAG, APPLICATION_NAME)
            .register(meterRegistry);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
        @RequestBody RegisterRequestDTO user
    ) {
        userRegistrationCounter.increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            if (
                user.getEmail() == null ||
                user.getPassword() == null ||
                user.getConfirmPassword() == null ||
                user.getUsername() == null ||
                user.getAccountType() == null ||
                !user.getPassword().equals(user.getConfirmPassword())
            ) {
                sample.stop(
                    Timer.builder(REGISTRATION_LATENCY)
                        .tag(STATUS_TAG, STATUS_FAILURE)
                        .register(meterRegistry)
                );
                return ResponseEntity.badRequest().build();
            }
            System.out.println("Registering user: " + user.getEmail());
            User registeredUser = userService.registerUser(user);
            sample.stop(
                Timer.builder(REGISTRATION_LATENCY)
                    .tag(STATUS_TAG, STATUS_SUCCESS)
                    .register(meterRegistry)
            );
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            sample.stop(
                Timer.builder(REGISTRATION_LATENCY)
                    .tag(STATUS_TAG, STATUS_FAILURE)
                    .register(meterRegistry)
            );
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(
        @RequestBody LoginRequestDTO loginRequest
    ) {
        loginCounter.increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            User user = userService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            );

            Authentication auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
            );
            String token = jwtTokenProvider.generateToken(auth);

            ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Strict")
                .build();

            sample.stop(
                Timer.builder(LOGIN_LATENCY)
                    .tag(STATUS_TAG, STATUS_SUCCESS)
                    .register(meterRegistry)
            );
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(user);
        } catch (RuntimeException e) {
            sample.stop(
                Timer.builder(LOGIN_LATENCY)
                    .tag(STATUS_TAG, STATUS_FAILURE)
                    .register(meterRegistry)
            );
            System.out.println("Login failed: " + e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      User me = userService.getUserByUsername(auth.getName());
      return ResponseEntity.ok(me);
    }
}

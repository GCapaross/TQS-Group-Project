package nikev.group.project.chargingplatform.security;

import java.sql.Time;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;
import nikev.group.project.chargingplatform.controller.BookingController;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Generates and validates JWT tokens.
 */
@Component
public class JwtTokenProvider {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION_MS}")
    private long jwtExpirationMs;

    private SecretKey secretKey;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        log.info("Now: {}", now);
        log.info("Expiry Date: {}", expiryDate);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        String username = getUsernameFromJwt(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}

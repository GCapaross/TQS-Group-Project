package nikev.group.project.chargingplatform.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);
    }

    @Test
    void whenGettingAuthorities_thenReturnsCorrectRole() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("USER")));
    }

    @Test
    void whenChangingRole_thenAuthoritiesAreUpdated() {
        user.setRole(Role.OPERATOR);
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("OPERATOR")));
    }

    @Test
    void whenCreatingUserWithAllArgsConstructor_thenAllFieldsAreSet() {
        User newUser = new User(1L, "testuser", "test@example.com", "password123", "1234-5678", Role.USER);
        
        assertEquals(1L, newUser.getId());
        assertEquals("testuser", newUser.getUsername());
        assertEquals("test@example.com", newUser.getEmail());
        assertEquals("password123", newUser.getPassword());
        assertEquals("1234-5678", newUser.getCredit_card());
        assertEquals(Role.USER, newUser.getRole());
    }

    @Test
    void whenCreatingUserWithNoArgsConstructor_thenFieldsAreNull() {
        User newUser = new User();
        
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getEmail());
        assertNull(newUser.getPassword());
        assertNull(newUser.getCredit_card());
        assertNull(newUser.getRole());
    }

    @Test
    void whenSettingAndGettingFields_thenValuesAreCorrect() {
        user.setCredit_card("1234-5678");
        user.setRole(Role.OPERATOR);
        
        assertEquals("1234-5678", user.getCredit_card());
        assertEquals(Role.OPERATOR, user.getRole());
    }

    @Test
    void whenComparingUsers_thenEqualsAndHashCodeWorkCorrectly() {
        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("testuser");
        user2.setEmail("test@example.com");
        user2.setPassword("password123");
        user2.setRole(Role.USER);
        
        assertEquals(user, user2);
        assertEquals(user.hashCode(), user2.hashCode());
    }
} 
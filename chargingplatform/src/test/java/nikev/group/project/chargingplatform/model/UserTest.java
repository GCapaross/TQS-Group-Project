package nikev.group.project.chargingplatform.model;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void testUserCreation() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedPassword");

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getPassword()).isEqualTo("hashedPassword");
    }

    @Test
    void testUserEquality() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("john.doe@example.com");

        User user2 = new User();
        user2.setId(1L);
        user2.setEmail("john.doe@example.com");

        // Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void testUserChargingSessions() {
        // Given
        User user = new User();
        user.setId(1L);

        Reservation session1 = new Reservation();
        session1.setId(1L);
        session1.setUser(user);

        Reservation session2 = new Reservation();
        session2.setId(2L);
        session2.setUser(user);

        // When
        user.setChargingSessions(List.of(session1, session2));

        // Then
        assertThat(user.getChargingSessions()).hasSize(2);
        assertThat(user.getChargingSessions().get(0)).isEqualTo(session1);
        assertThat(user.getChargingSessions().get(1)).isEqualTo(session2);
    }
} 
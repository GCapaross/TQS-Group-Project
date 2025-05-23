package nikev.group.project.chargingplatform.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StationReviewTest {

    @Test
    void testStationReviewCreation() {
        // Given
        User user = new User();
        user.setId(1L);

        Station station = new Station();
        station.setId(1L);

        StationReview review = new StationReview();
        review.setId(1L);
        review.setUser(user);
        review.setChargingStation(station);
        review.setRating(5);
        review.setComment("Great station!");
        review.setCreatedAt(LocalDateTime.now());

        // Then
        assertThat(review.getId()).isEqualTo(1L);
        assertThat(review.getUser()).isEqualTo(user);
        assertThat(review.getChargingStation()).isEqualTo(station);
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Great station!");
        assertThat(review.getCreatedAt()).isNotNull();
    }

    @Test
    void testStationReviewEquality() {
        // Given
        StationReview review1 = new StationReview();
        review1.setId(1L);
        review1.setRating(5);
        review1.setComment("Great station!");

        StationReview review2 = new StationReview();
        review2.setId(1L);
        review2.setRating(5);
        review2.setComment("Great station!");

        // Then
        assertThat(review1).isEqualTo(review2);
        assertThat(review1.hashCode()).isEqualTo(review2.hashCode());
    }

    @Test
    void testStationReviewPrePersist() {
        // Given
        StationReview review = new StationReview();
        review.setRating(5);
        review.setComment("Great station!");

        // When
        review.onCreate();

        // Then
        assertThat(review.getCreatedAt()).isNotNull();
    }
} 
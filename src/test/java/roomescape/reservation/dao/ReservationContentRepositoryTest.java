package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.ReservationContent;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@DataJpaTest
class ReservationContentRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationContentRepository reservationContentRepository;

    @Test
    @DisplayName("테마, 시간, 일자의 조합으로 ReservationContent를 DB에서 조회한다.")
    void findByThemeAndTimeAndDate_ShouldReturnReservationContent_WhenThemeAndTimeAndDateIsGiven() {
        // Given
        Theme targetTheme = new Theme("dobby 테마", "테마 설명", "test.jpg");
        Time targetTime = new Time(LocalTime.of(12, 0));
        LocalDate targetDate = LocalDate.now();
        ReservationContent reservationContent = new ReservationContent(targetDate, targetTime, targetTheme);

        // When
        entityManager.persist(targetTheme);
        entityManager.persist(targetTime);
        entityManager.flush();
        ReservationContent expectedReservationContent = entityManager.merge(reservationContent);

        // Then
        ReservationContent actualReservationContent = reservationContentRepository.findByThemeAndTimeAndDate(
                        targetTheme, targetTime, targetDate)
                .orElseThrow(NullPointerException::new);
        assertThat(actualReservationContent).isEqualTo(expectedReservationContent);
    }

}

package roomescape.reservation.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.Status;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void countByDateAndTimeIdAndThemeIdAndStatusIsZero() {
        int count = reservationRepository.countByDateAndTimeIdAndThemeIdAndStatus(
                LocalDate.parse("2024-12-23"), 1, 1, Status.PENDING
        );

        Assertions.assertThat(count).isEqualTo(0);
    }

    @Test
    void countByDateAndTimeIdAndThemeIdAndStatusIsOne() {
        int count = reservationRepository.countByDateAndTimeIdAndThemeIdAndStatus(
                LocalDate.parse("2024-12-23"), 2, 3, Status.PENDING
        );

        Assertions.assertThat(count).isEqualTo(1);
    }
}

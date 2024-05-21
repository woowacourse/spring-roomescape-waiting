package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Optional;
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
    void countAlreadyRegisteredWaitingsIsZero() {
        int count = reservationRepository.countAlreadyRegisteredWaitings(
                4, LocalDate.parse("2024-12-23"), 2, 3, Status.PENDING
        );

        assertThat(count).isEqualTo(0);
    }

    @Test
    void countAlreadyRegisteredWaitingsIsOne() {
        int count = reservationRepository.countAlreadyRegisteredWaitings(
                5, LocalDate.parse("2024-12-23"), 2, 3, Status.PENDING
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void earliestRegisteredWaiting() {
        Optional<Long> waitingId = reservationRepository.findEarliestRegisteredWaiting(
                LocalDate.parse("2024-12-23"), 2, 3, Status.PENDING
        );

        assertTrue(waitingId.isPresent());
        assertThat(waitingId.get()).isEqualTo(4);
    }

    @Test
    void earliestRegisteredWaitingWhenDoesNotHaveWaiting() {
        Optional<Long> waitingId = reservationRepository.findEarliestRegisteredWaiting(
                LocalDate.parse("2024-12-23"), 1, 1, Status.PENDING
        );

        assertTrue(waitingId.isEmpty());
    }
}

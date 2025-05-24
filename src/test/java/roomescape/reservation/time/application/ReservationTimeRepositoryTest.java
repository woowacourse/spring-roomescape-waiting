package roomescape.reservation.time.application;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.time.domain.ReservationTimeFixtures;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @ParameterizedTest
    @DisplayName("시작 시간으로 예약 시간 존재 여부를 확인한다")
    @CsvSource({
            "15:00, true",
            "16:00, false"
    })
    void existsByStartAt(LocalTime givenTime, boolean expected) {
        // given
        LocalTime existingTime = LocalTime.of(15, 0);
        ReservationTimeFixtures.persistReservationTime(entityManager, existingTime);
        flushAndClear();

        // when
        boolean result = reservationTimeRepository.existsByStartAt(givenTime);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}

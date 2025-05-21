package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatNoException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.time.domain.ReservationTime;

@ActiveProfiles("test")
@DataJpaTest
class ReservationTimeTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("테이블 생성 테스트")
    void createReservationTimeTest() {
        assertThatNoException()
                .isThrownBy(() -> entityManager.persist(new ReservationTime(LocalTime.MIDNIGHT)));
    }
}

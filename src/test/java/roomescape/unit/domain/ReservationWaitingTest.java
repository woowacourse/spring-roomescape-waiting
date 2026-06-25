package roomescape.unit.domain;

import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.fixture.ReservationFixture;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationWaitingTest {

    private static final Reservation RESERVATION = ReservationFixture.create();

    @Test
    void 대기자_이름과_같으면_isOwnedBy가_true를_반환한다() {
        ReservationWaiting waiting = new ReservationWaiting("민욱", LocalDateTime.of(2026, 8, 1, 10, 0), RESERVATION);

        assertThat(waiting.isOwnedBy("민욱")).isTrue();
    }

    @Test
    void 대기자_이름과_다르면_isOwnedBy가_false를_반환한다() {
        ReservationWaiting waiting = new ReservationWaiting("민욱", LocalDateTime.of(2026, 8, 1, 10, 0), RESERVATION);

        assertThat(waiting.isOwnedBy("브라운")).isFalse();
    }
}

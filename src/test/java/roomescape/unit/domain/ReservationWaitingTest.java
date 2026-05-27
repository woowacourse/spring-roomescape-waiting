package roomescape.unit.domain;

import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationWaitingTest {

    private static final Reservation RESERVATION = new Reservation(
            1L,
            "티뉴",
            LocalDate.of(2026, 8, 5),
            new ReservationTime(1L, LocalTime.of(10, 0)),
            new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg")
    );

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

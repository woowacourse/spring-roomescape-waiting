package roomescape.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.UnauthorizedException;

class ReservationWaitingTest {

    private static final Reservation RESERVATION = new Reservation(
            1L,
            "티뉴",
            LocalDate.of(2026, 8, 5),
            new ReservationTime(1L, LocalTime.of(10, 0)),
            new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg")
    );

    private static final Reservation PAST_RESERVATION = new Reservation(
            1L,
            "티뉴",
            LocalDate.of(2020, 1, 1),
            new ReservationTime(1L, LocalTime.of(10, 0)),
            new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg")
    );

    @Test
    void 본인이_예약한_슬롯에는_대기를_생성할_수_없다() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 1, 10, 0);

        assertThatThrownBy(() -> ReservationWaiting.createWith("티뉴", now, RESERVATION))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("본인이 예약한 슬롯");
    }

    @Test
    void 지난_예약에는_대기를_생성할_수_없다() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 1, 10, 0);

        assertThatThrownBy(() -> ReservationWaiting.createWith("민욱", now, PAST_RESERVATION))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("지난 시각에는 대기");
    }

    @Test
    void 다른_사람의_미래_예약에는_대기를_생성할_수_있다() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 1, 10, 0);

        ReservationWaiting waiting = ReservationWaiting.createWith("민욱", now, RESERVATION);

        assertThat(waiting.getName()).isEqualTo("민욱");
        assertThat(waiting.getCreatedAt()).isEqualTo(now);
        assertThat(waiting.getReservation()).isEqualTo(RESERVATION);
    }

    @Test
    void 본인_대기가_아니면_취소할_수_없다() {
        ReservationWaiting waiting = new ReservationWaiting("민욱", LocalDateTime.of(2026, 8, 1, 10, 0), RESERVATION);

        assertThatThrownBy(() -> waiting.cancelBy("브라운"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("본인의 예약 대기");
    }

    @Test
    void 본인_대기는_취소할_수_있다() {
        ReservationWaiting waiting = new ReservationWaiting("민욱", LocalDateTime.of(2026, 8, 1, 10, 0), RESERVATION);

        waiting.cancelBy("민욱");
    }
}

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
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;

class ReservationWaitingTest {

    private static final ReservationTime RESERVATION_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");
    private static final LocalDateTime WAITING_CREATED_AT = LocalDateTime.of(2026, 8, 1, 10, 0);

    private static final Reservation RESERVATION = new Reservation(
            1L,
            "티뉴",
            LocalDate.of(2026, 8, 5),
            RESERVATION_TIME,
            THEME
    );

    private static final Reservation PAST_RESERVATION = new Reservation(
            1L,
            "티뉴",
            LocalDate.of(2020, 1, 1),
            RESERVATION_TIME,
            THEME
    );

    @Test
    void 본인이_예약한_슬롯에는_대기를_생성할_수_없다() {
        assertThatThrownBy(() -> ReservationWaiting.createWith("티뉴", WAITING_CREATED_AT, RESERVATION))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("본인이 예약한 슬롯");
    }

    @Test
    void 지난_예약에는_대기를_생성할_수_없다() {
        assertThatThrownBy(() -> ReservationWaiting.createWith("민욱", WAITING_CREATED_AT, PAST_RESERVATION))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("지난 시각에는 대기");
    }

    @Test
    void 다른_사람의_미래_예약에는_대기를_생성할_수_있다() {
        ReservationWaiting waiting = ReservationWaiting.createWith("민욱", WAITING_CREATED_AT, RESERVATION);

        assertThat(waiting.getName()).isEqualTo("민욱");
        assertThat(waiting.getCreatedAt()).isEqualTo(WAITING_CREATED_AT);
        assertThat(waiting.getReservation()).isEqualTo(RESERVATION);
    }

    @Test
    void 본인_대기가_아니면_취소할_수_없다() {
        ReservationWaiting waiting = new ReservationWaiting("민욱", WAITING_CREATED_AT, RESERVATION);

        assertThatThrownBy(() -> waiting.cancelBy("브라운"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("본인의 예약 대기");
    }

    @Test
    void 본인_대기는_취소할_수_있다() {
        ReservationWaiting waiting = new ReservationWaiting("민욱", WAITING_CREATED_AT, RESERVATION);

        waiting.cancelBy("민욱");
    }
}

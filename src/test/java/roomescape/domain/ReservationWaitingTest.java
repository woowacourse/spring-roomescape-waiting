package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;

class ReservationWaitingTest {

    @Test
    @DisplayName("예약 대기를 생성한다")
    void createNew() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        assertThatCode(() -> ReservationWaiting.createNew(
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("지난 예약에는 대기를 생성할 수 없다")
    void createNewPastReservation() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        assertThatThrownBy(() -> ReservationWaiting.createNew(
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-06T10:01:00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지난 예약에는 대기를 생성할 수 없습니다.");
    }

    private Reservation createReservation(final LocalDate date, final LocalTime time) {
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime reservationTime = ReservationTime.of(1L, time);

        return Reservation.of(1L, "쿠다", date, theme, reservationTime);
    }
}

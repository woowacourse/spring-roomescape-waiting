package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.time.ReservationTime;
import roomescape.waiting.ReservationWaiting;

class ReservationTest {

    @Test
    void 같은_일정의_대기자를_예약자로_승인한다() {
        Long reservationId = 1L;
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Reservation reservation = new Reservation(reservationId, "기존 예약자", themeId, date, time);
        ReservationWaiting waiting = new ReservationWaiting(
                2L,
                "첫 번째 대기자",
                themeId,
                date,
                time,
                LocalDateTime.now(),
                1L
        );

        Reservation approvedReservation = reservation.approve(waiting);

        assertThat(approvedReservation.getId()).isEqualTo(reservationId);
        assertThat(approvedReservation.getName()).isEqualTo(waiting.getName());
        assertThat(approvedReservation.getThemeId()).isEqualTo(themeId);
        assertThat(approvedReservation.getDate()).isEqualTo(date);
        assertThat(approvedReservation.getTime()).isEqualTo(time);
    }

    @Test
    void 다른_일정의_대기자는_예약자로_승인할_수_없다() {
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Reservation reservation = new Reservation(1L, "기존 예약자", themeId, date, reservationTime);
        ReservationWaiting waiting = new ReservationWaiting(
                2L,
                "첫 번째 대기자",
                themeId,
                date,
                new ReservationTime(2L, LocalTime.of(11, 0)),
                LocalDateTime.now(),
                1L
        );

        assertThatThrownBy(() -> reservation.approve(waiting))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.INVALID_RESERVATION_WAITING.getMessage());
    }
}

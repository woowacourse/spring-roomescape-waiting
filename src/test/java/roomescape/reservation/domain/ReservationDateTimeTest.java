package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.exception.PastReservationException;
import roomescape.time.domain.ReservationTime;

class ReservationDateTimeTest {

    @Test
    void 현재_이전_시간에는_예약할_수_없다() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        ReservationDate reservationDate = new ReservationDate(today);
        LocalTime time = now.toLocalTime().minusMinutes(1);
        ReservationTime reservationTime = ReservationTime.open(time);

        assertThatThrownBy(() -> ReservationDateTime.create(reservationDate, reservationTime))
                .isInstanceOf(PastReservationException.class)
                .hasMessage("[ERROR] 지난 날짜에는 예약할 수 없습니다.");
    }

    @Test
    void 현재_이후_시간에는_예약이_성공한다() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        ReservationDate reservationDate = new ReservationDate(today);
        ReservationTime futureTime = ReservationTime.open(now.toLocalTime().plusMinutes(10));

        assertThatCode(() -> ReservationDateTime.create(reservationDate, futureTime))
                .doesNotThrowAnyException();
    }
}

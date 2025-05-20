package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.common.Constant.TOMORROW;
import static roomescape.common.Constant.YESTERDAY;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.exception.PastReservationException;
import roomescape.time.domain.ReservationTime;

public class ReservationDateTimeTest {

    @Test
    void 현재_이전_시간에는_예약할_수_없다() {
        LocalDate date = YESTERDAY.toLocalDate();
        ReservationDate reservationDate = new ReservationDate(date);
        LocalTime time = YESTERDAY.toLocalTime();
        ReservationTime reservationTime = ReservationTime.create(time);

        assertThatThrownBy(() -> new ReservationDateTime(reservationDate, reservationTime))
                .isInstanceOf(PastReservationException.class);
    }

    @Test
    void 현재_이후_시간에는_예약이_성공한다() {
        LocalDate today = TOMORROW.toLocalDate();
        ReservationDate reservationDate = new ReservationDate(today);
        ReservationTime futureTime = ReservationTime.create(TOMORROW.toLocalTime());

        assertThatCode(() -> new ReservationDateTime(reservationDate, futureTime))
                .doesNotThrowAnyException();
    }
}

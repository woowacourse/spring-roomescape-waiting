package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.*;
import static roomescape.common.Constant.FIXED_CLOCK;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.time.ReservationTime;

class ReservationDateTimeTest {

    @Test
    void 현재_이전_시간에는_예약할_수_없다() {
        LocalDateTime now = LocalDateTime.now(FIXED_CLOCK);
        ReservationDate today = new ReservationDate(now.toLocalDate());
        ReservationTime pastTime = new ReservationTime(1L, now.toLocalTime().minusHours(1));

        assertThatThrownBy(() -> new ReservationDateTime(today, pastTime, FIXED_CLOCK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 현재_이후_시간에는_예약이_성공한다() {
        LocalDateTime now = LocalDateTime.now(FIXED_CLOCK);
        ReservationDate today = new ReservationDate(now.toLocalDate());
        ReservationTime futureTime = new ReservationTime(1L, now.toLocalTime().plusHours(1));

        assertThatCode(() -> new ReservationDateTime(today, futureTime, FIXED_CLOCK))
                .doesNotThrowAnyException();
    }

}

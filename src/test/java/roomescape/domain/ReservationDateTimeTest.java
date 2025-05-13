package roomescape.domain;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.PastReservationException;
import roomescape.fake.FixedCurrentTimeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationDateTimeTest {

    @Test
    void 현재_이전_시간에는_예약할_수_없다() {
        LocalDateTime now = new FixedCurrentTimeService().now();
        LocalDate today = now.toLocalDate();
        ReservationDate reservationDate = new ReservationDate(today);
        LocalTime time = now.toLocalTime().minusMinutes(1);
        ReservationTime reservationTime = ReservationTime.create(time);

        assertThatThrownBy(() -> ReservationDateTime.create(reservationDate, reservationTime, now))
                .isInstanceOf(PastReservationException.class);
    }

    @Test
    void 현재_이후_시간에는_예약이_성공한다() {
        LocalDateTime now = new FixedCurrentTimeService().now();
        LocalDate today = now.toLocalDate();
        ReservationDate reservationDate = new ReservationDate(today);
        ReservationTime futureTime = ReservationTime.create(now.toLocalTime().plusMinutes(10));

        assertThatCode(() -> ReservationDateTime.create(reservationDate, futureTime, now))
                .doesNotThrowAnyException();
    }
}

package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

class ReservationTimeTest {

    @Test
    void 예약_날짜가_내일이면_정상_처리() {
        ReservationTime time = ReservationTime.of(
            1L,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );

        assertThatCode(() -> time.validateIfTimePast(LocalDate.now().plusDays(1)))
            .doesNotThrowAnyException();
    }

    @Test
    void 예약_날짜는_같지만_시간이_미래면_정상_처리() {
        ReservationTime time = ReservationTime.of(
            1L,
            LocalTime.now().plusHours(1),
            LocalTime.now().plusHours(2)
        );

        assertThatCode(() -> time.validateIfTimePast(LocalDate.now()))
            .doesNotThrowAnyException();
    }

    @Test
    void 예약_날짜가_과거이면_예외() {
        ReservationTime time = ReservationTime.of(
            1L,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );

        assertThatThrownBy(() -> time.validateIfTimePast(LocalDate.now().minusDays(1)))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_TIME_PASSED);
    }

    @Test
    void 예약_날짜가_오늘이고_시작_시간이_과거면_예외() {
        ReservationTime time = ReservationTime.of(
            1L,
            LocalTime.now().minusHours(1),
            LocalTime.now().plusHours(1)
        );

        assertThatThrownBy(() -> time.validateIfTimePast(LocalDate.now()))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_TIME_PASSED);
    }
}
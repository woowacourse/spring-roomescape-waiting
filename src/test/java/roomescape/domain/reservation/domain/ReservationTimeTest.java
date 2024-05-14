package roomescape.domain.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.LocalTimeFixture.TEN_HOUR;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.global.exception.EscapeApplicationException;

class ReservationTimeTest {

    @DisplayName("time이 정상적인 값일 경우 예외가 발생하지 않습니다.")
    @Test
    void should_not_throw_exception_when_reservation_time_is_right() {
        assertThatCode(() -> new ReservationTime(null, TEN_HOUR))
                .doesNotThrowAnyException();
    }

    @DisplayName("time이 null인 경우 ReservationTime 생성 시 예외가 발생합니다")
    @Test
    void should_throw_ClientIllegalArgumentException_when_time_is_null() {
        assertThatThrownBy(() -> new ReservationTime(null, null))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("예약 가능 시각은 null일 수 없습니다");
    }
}

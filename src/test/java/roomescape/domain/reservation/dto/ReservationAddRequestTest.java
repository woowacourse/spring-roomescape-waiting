package roomescape.domain.reservation.dto;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.LocalDateFixture.AFTER_ONE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.BEFORE_ONE_DAYS_DATE;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.EscapeApplicationException;

class ReservationAddRequestTest {

    @DisplayName("정상적인 값이면 ReservationAddRequest생성 시 예외가 발생하지 않는다")
    @Test
    void should_not_throw_exception_when_request_is_right() {
        assertThatCode(() -> new ReservationAddRequest(AFTER_ONE_DAYS_DATE, 1L, 1L, 1L));
    }

    @DisplayName("date가 현재 날짜 보다 이전 날짜이면 ReservationAddRequest생성 시 예외가 발생한다")
    @Test
    void should_throw_ClientIllegalArgumentException_when_date_is_past() {
        assertThatThrownBy(() -> new ReservationAddRequest(BEFORE_ONE_DAYS_DATE, 1L, 1L, 1L))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage(BEFORE_ONE_DAYS_DATE + ": 예약 날짜는 현재 보다 이전일 수 없습니다");
    }
}

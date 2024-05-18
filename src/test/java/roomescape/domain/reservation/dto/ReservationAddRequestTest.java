package roomescape.domain.reservation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationAddRequestTest {

    @DisplayName("정상적인 값이면 ReservationAddRequest생성 시 예외가 발생하지 않는다")
    @Test
    void should_not_throw_exception_when_request_is_right() {
        assertThatCode(() -> new ReservationAddRequest(LocalDate.MAX, 1L, 1L, 1L))
                .doesNotThrowAnyException();
    }

    @DisplayName("date가 현재 날짜 보다 이전 날짜이면 ReservationAddRequest생성 시 예외가 발생한다")
    @Test
    void should_throw_ClientIllegalArgumentException_when_date_is_past() {
        assertThatThrownBy(() -> new ReservationAddRequest(LocalDate.of(2000, 1, 1), 1L, 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약 날짜는 현재 보다 이전일 수 없습니다");
    }
}

package roomescape.reservation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeTest {
    @DisplayName("예약 시간에 공백을 입력하면 예외를 발생한다.")
    @Test
    void throwExceptionWhenReservationTimeBlank() {
        // When & Then
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시간 정보는 공백을 입력할 수 없습니다.");
    }
}

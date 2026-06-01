package roomescape.reservationtime.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.RoomEscapeException;

class ReservationTimeTest {

    @DisplayName("예약 시간이 비어있을 때 예외 발생을 테스트합니다.")
    @Test
    void validate_start_at() {
        assertThatThrownBy(() -> ReservationTime.builder()
                .startAt(null)
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("시간은 비어있을 수 없습니다.");
    }
}

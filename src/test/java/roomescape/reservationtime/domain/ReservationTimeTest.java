package roomescape.reservationtime.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("시작 시간이 null이면 예외 발생")
    void 시작시간_null_예외() {
        assertThatThrownBy(() -> ReservationTime.of(null, LocalTime.of(11, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 시간은 필수입니다.");
    }

    @Test
    @DisplayName("종료 시간이 null이면 예외 발생")
    void 종료시간_null_예외() {
        assertThatThrownBy(() -> ReservationTime.of(LocalTime.of(10, 0), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시간은 필수입니다.");
    }
}

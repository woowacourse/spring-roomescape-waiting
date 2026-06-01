package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Time;

class ReservationTimeTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 예약 시간 객체가 생성된다.")
    void create_ValidParameters_CreatesReservationTime() {
        Time time = new Time(1L, LocalTime.of(10, 0));
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("시작 시간이 null이면 예외가 발생한다.")
    void create_NullStartAt_ThrowsException() {
        assertThatThrownBy(() -> new Time(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

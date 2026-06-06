package roomescape.reservationtime.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("정상 시간 생성")
    void 정상_시간_생성() {
        ReservationTime time = ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(time.getFinishAt()).isEqualTo(LocalTime.of(11, 0));
    }

    @Test
    @DisplayName("시작 시간이 null이면 예외 발생")
    void 시작시간_null_예외() {
        assertThatThrownBy(() -> ReservationTime.of(null, LocalTime.of(11, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("종료 시간이 null이면 예외 발생")
    void 종료시간_null_예외() {
        assertThatThrownBy(() -> ReservationTime.of(LocalTime.of(10, 0), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

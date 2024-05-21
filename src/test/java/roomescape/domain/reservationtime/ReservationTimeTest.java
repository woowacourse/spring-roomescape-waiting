package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void create() {
        assertThatCode(() -> new ReservationTime(LocalTime.of(10, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("시작 시간이 없으면 예외가 발생한다.")
    void validateStartAt() {
        assertThatThrownBy(() -> new ReservationTime(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 시간은 필수 값입니다.");
    }
}

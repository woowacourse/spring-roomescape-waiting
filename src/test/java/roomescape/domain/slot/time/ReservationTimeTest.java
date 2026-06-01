package roomescape.domain.slot.time;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("올바른 정보로 시간을 생성하면 성공한다.")
    void createReservationTime_Success() {
        Long id = 1L;
        LocalTime startAt = LocalTime.of(10, 0);

        assertDoesNotThrow(() -> new ReservationTime(id, startAt));
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다")
    void createReservationTime_WhenTimeIsNull_ThrowException() {
        Long id = 1L;
        LocalTime startAt = null;

        // when & then
        assertThatThrownBy(() -> new ReservationTime(id, startAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약 시간이 비어 있습니다.");
    }
}

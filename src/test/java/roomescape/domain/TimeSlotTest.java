package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.timeslot.TimeSlot;

class TimeSlotTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 예약 시간 객체가 생성된다.")
    void 예약_시간_생성() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        assertThat(timeSlot.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("시작 시간이 null이면 예외가 발생한다.")
    void 시작_시간_null_예외_발생() {
        assertThatThrownBy(() -> new TimeSlot(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

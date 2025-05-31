package roomescape.domain.timeslot;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeSlotTest {

    @Test
    @DisplayName("주어진 시간이 시작 시간보다 늦은지 확인할 수 있다.")
    void isTimeBefore() {
        // given
        var timeSlot = TimeSlot.register(LocalTime.of(10, 0));
        var earlierTime = LocalTime.of(9, 0);
        var laterTime = LocalTime.of(11, 0);

        // when & then
        assertThat(timeSlot.isTimeBefore(earlierTime)).isFalse();
        assertThat(timeSlot.isTimeBefore(laterTime)).isTrue();
    }
}

package roomescape.domain.timeslot;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeSlotTest {

    @Test
    @DisplayName("타임 슬롯이 주어진 시간보다 이전인지 확인한다.")
    void isTimeBefore() {
        // given
        var _10_00 = LocalTime.of(10, 0);
        var timeSlot = new TimeSlot(1L, _10_00);

        // when
        var _11_00 = LocalTime.of(11, 0);
        var isBefore = timeSlot.isTimeBefore(_11_00);

        // then
        assertThat(isBefore).isTrue();
    }
}

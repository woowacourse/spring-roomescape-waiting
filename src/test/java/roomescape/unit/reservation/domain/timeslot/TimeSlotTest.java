package roomescape.unit.reservation.domain.timeslot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.timeslot.TimeSlot;

class TimeSlotTest {

    @DisplayName("주어진 시간보다 이전 시간인지 여부를 반환한다.")
    @Test
    void isBefore() {
        // given
        final LocalTime time = LocalTime.of(10, 0);
        final TimeSlot timeSlot = new TimeSlot(1L, time);
        // when & then
        final LocalTime afterTime = time.plusMinutes(1);
        assertAll(
                () -> assertThat(timeSlot.isBefore(time)).isTrue(),
                () -> assertThat(timeSlot.isBefore(afterTime)).isTrue()
        );
    }
}

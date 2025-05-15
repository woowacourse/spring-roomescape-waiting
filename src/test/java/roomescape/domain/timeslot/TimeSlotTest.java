package roomescape.domain.timeslot;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeSlotTest {

    @Test
    @DisplayName("타임 슬롯이 주어진 시간보다 이전인지 확인한다")
    void isTimeBefore() {
        // given
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));

        // when
        boolean isBefore = timeSlot.isTimeBefore(LocalTime.of(11, 0));

        // then
        assertThat(isBefore).isTrue();
    }

    @Test
    @DisplayName("타임 슬롯이 주어진 타임 슬롯과 같은 시간인지 확인한다")
    void isSameDateTime() {
        // given
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        TimeSlot otherTimeSlot = new TimeSlot(1L, LocalTime.of(10, 0));

        // when
        boolean isSameDateTime = timeSlot.isSameAs(otherTimeSlot);

        // then
        assertThat(isSameDateTime).isTrue();
    }
}

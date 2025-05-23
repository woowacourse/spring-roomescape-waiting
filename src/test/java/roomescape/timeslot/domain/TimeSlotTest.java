package roomescape.timeslot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidArgumentException;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class TimeSlotTest {

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다")
    void validateNullTime() {
        // when
        // then
        assertThatThrownBy(() -> TimeSlot.withoutId(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking null]: TimeSlot.startAt");

        assertThatThrownBy(() -> TimeSlot.withId(TimeSlotId.from(1L), null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking null]: TimeSlot.startAt");
    }

    @Test
    @DisplayName("유효한 시간으로 ReservationTime 객체를 생성할 수 있다")
    void createValidReservationTime() {
        // given
        final LocalTime time = LocalTime.of(10, 0);

        // when
        final TimeSlot timeSlot = TimeSlot.withoutId(ReservationTime.from(time));

        // then
        assertAll(() -> {
            assertThat(timeSlot).isNotNull();
            assertThat(timeSlot.getStartAt().getValue()).isEqualTo(time);
        });
    }

    @Test
    @DisplayName("isBefore 메서드는 주어진 시간과 예약 시간을 비교한다")
    void testIsBefore() {
        // given
        final LocalTime time = LocalTime.of(10, 0);
        final TimeSlot timeSlot = TimeSlot.withoutId(ReservationTime.from(time));

        // when & then
        assertAll(() -> {
            assertThat(timeSlot.isBefore(LocalTime.of(11, 0))).isTrue();
            assertThat(timeSlot.isBefore(LocalTime.of(9, 0))).isFalse();
            assertThat(timeSlot.isBefore(LocalTime.of(10, 0))).isFalse();
        });
    }
}

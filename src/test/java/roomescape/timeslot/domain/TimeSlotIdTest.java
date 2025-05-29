package roomescape.timeslot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidArgumentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class TimeSlotIdTest {

    @Test
    @DisplayName("예약 시간 ID가 null이면 예외가 발생한다")
    void validateNullReservationTimeId() {
        // when
        // then
        assertThatThrownBy(() -> TimeSlotId.from(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking null]: EntityId.value");
    }

    @Test
    @DisplayName("유효한 ID로 ReservationTimeId 객체를 생성할 수 있다")
    void createValidReservationTimeId() {
        // given
        final Long id = 1L;

        // when
        final TimeSlotId timeSlotId = TimeSlotId.from(id);

        // then
        assertAll(() -> {
            assertThat(timeSlotId).isNotNull();
            assertThat(timeSlotId.getValue()).isEqualTo(id);
        });
    }
} 

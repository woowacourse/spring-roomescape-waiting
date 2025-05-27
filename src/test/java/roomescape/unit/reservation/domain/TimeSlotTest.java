package roomescape.unit.reservation.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.exception.ArgumentNullException;
import roomescape.reservation.domain.TimeSlot;

class TimeSlotTest {

    @Test
    void 시작시간이_null이면_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> TimeSlot.builder().id(1L).build())
                .isInstanceOf(ArgumentNullException.class);
    }
}
package roomescape.unit.domain.time;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.time.AvailableReservationTime;

class AvailableReservationTimeTest {

    @Test
    void 시간은_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new AvailableReservationTime(null, false))
                .isInstanceOf(NullPointerException.class);
    }
}

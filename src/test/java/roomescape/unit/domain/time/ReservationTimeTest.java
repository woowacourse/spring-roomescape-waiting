package roomescape.unit.domain.time;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import roomescape.domain.time.ReservationTime;

class ReservationTimeTest {

    @Test
    void startAt은_null일_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(NullPointerException.class);
    }
}

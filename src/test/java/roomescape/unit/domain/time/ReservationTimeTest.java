package roomescape.unit.domain.time;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.time.ReservationTime;

class ReservationTimeTest {

    @Test
    void id는_null일_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(null, LocalTime.of(10, 0)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void startAt은_null일_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(NullPointerException.class);
    }
}

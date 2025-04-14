package roomescape.reservation.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.time.domain.exception.ReservationTimeNullException;
import roomescape.time.domain.ReservationTime;

public class ReservationTimeTest {

    @Test
    void 시간은_null이_될_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(ReservationTimeNullException.class);
    }
}

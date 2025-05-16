package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.reservationtime.ReservationTimeFieldRequiredException;

class ReservationTimeTest {

    @DisplayName("시작시간이 null일 수 없다")
    @Test
    void reservationStartAtTest() {
        // given
        LocalTime time = null;
        // when & then
        assertThatThrownBy(() -> new ReservationTime(time))
                .isInstanceOf(ReservationTimeFieldRequiredException.class);
    }
}

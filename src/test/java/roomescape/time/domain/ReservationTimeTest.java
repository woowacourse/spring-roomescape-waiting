package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @DisplayName("주어진 시간보다 이전 시간인지 여부를 반환한다.")
    @Test
    void testIsBeforeOrEqual() {
        // given
        LocalTime time = LocalTime.of(10, 0);
        ReservationTime reservationTime = new ReservationTime(1L, time);
        // when
        // then
        LocalTime afterTime = time.plusMinutes(1);
        assertThat(reservationTime.isBeforeOrEqual(afterTime)).isTrue();
        assertThat(reservationTime.isBeforeOrEqual(time)).isTrue();
    }
}

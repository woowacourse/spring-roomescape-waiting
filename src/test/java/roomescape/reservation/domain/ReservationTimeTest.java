package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @DisplayName("주어진 시간과 같거나 이전 시간인지 여부를 반환한다.")
    @Test
    void testIsBefore() {
        // given
        LocalTime time = LocalTime.of(10, 0);
        ReservationTime reservationTime = new ReservationTime(1L, time);
        LocalTime afterTime = time.plusMinutes(1);

        // when
        // then
        assertAll(
                () -> assertThat(reservationTime.isBefore(afterTime)).isTrue(),
                () -> assertThat(reservationTime.isBefore(time)).isTrue()
        );
    }
}

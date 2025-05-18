package roomescape.unit.reservation.domain.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.time.ReservationTime;

class ReservationTimeTest {

    @DisplayName("주어진 시간보다 이전 시간인지 여부를 반환한다.")
    @Test
    void isBefore() {
        // given
        final LocalTime time = LocalTime.of(10, 0);
        final ReservationTime reservationTime = new ReservationTime(1L, time);
        // when & then
        final LocalTime afterTime = time.plusMinutes(1);
        assertAll(
                () -> assertThat(reservationTime.isBefore(time)).isTrue(),
                () -> assertThat(reservationTime.isBefore(afterTime)).isTrue()
        );
    }
}

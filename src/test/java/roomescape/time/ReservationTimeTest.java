package roomescape.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationTime;

class ReservationTimeTest {

    @Test
    void 시간_생성() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

        assertThat(reservationTime.getId()).isEqualTo(1L);
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }
}

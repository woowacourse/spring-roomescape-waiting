package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.Time;

class TimeTest {

    @Test
    void 시간_생성() {
        Time time = new Time(1L, LocalTime.of(10, 0));

        assertThat(time.getId()).isEqualTo(1L);
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }
}

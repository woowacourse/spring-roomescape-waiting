package roomescape.service.dto;

import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvailableTimesTest {

    @Test
    void 전체_예약_시간과_예약된_시간_id로_예약_가능_여부를_계산한다() {
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.of(13, 0)),
                new ReservationTime(2L, LocalTime.of(15, 0)),
                new ReservationTime(3L, LocalTime.of(18, 0))
        );
        List<Long> reservedTimeIds = List.of(1L, 3L);

        AvailableTimes availableTimes = AvailableTimes.from(times, reservedTimeIds);

        assertThat(availableTimes.values()).containsExactly(
                new TimeAvailability(1L, LocalTime.of(13, 0), false),
                new TimeAvailability(2L, LocalTime.of(15, 0), true),
                new TimeAvailability(3L, LocalTime.of(18, 0), false)
        );
    }
}

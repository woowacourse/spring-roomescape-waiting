package roomescape.reservation.business.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    void 다른시간과_간섭을_확인한다() {
        // given
        final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        final Duration duration = Duration.ofHours(2);
        final LocalTime eight = LocalTime.of(8, 0);
        final LocalTime nine = LocalTime.of(9, 0);
        final LocalTime eleven = LocalTime.of(11, 0);
        final LocalTime twelve = LocalTime.of(12, 0);

        // when & then
        assertAll(
                () -> assertThat(reservationTime.hasConflict(duration, eight)).isFalse(),
                () -> assertThat(reservationTime.hasConflict(duration, nine)).isTrue(),
                () -> assertThat(reservationTime.hasConflict(duration, eleven)).isTrue(),
                () -> assertThat(reservationTime.hasConflict(duration, twelve)).isFalse()
        );
    }
}

package roomescape.unit.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.exception.PastDateTimeException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.TimeSlot;

class ReservationTimeTest {

    @Test
    void 예약_일시가_과거일_경우_예외가_발생한다() {
        // given
        TimeSlot timeSlot = TimeSlot.builder()
                .id(1L)
                .startAt(LocalTime.of(9, 0))
                .build();
        ReservationTime reservationTime = new ReservationTime(LocalDate.now().minusDays(1), timeSlot);
        // when & then
        Assertions.assertThatThrownBy(() -> reservationTime.validateDateTime())
                .isInstanceOf(PastDateTimeException.class);
    }
}
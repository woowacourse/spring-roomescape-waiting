package roomescape.domain.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DateUtils;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.BusinessRuleViolationException;

class ReservationDateTimeTest {

    @Test
    @DisplayName("새로운 예약 일시가 현재 일시보다 이전이면 예외가 발생한다.")
    void isBefore() {
        var yesterday = DateUtils.yesterday();
        var timeSlot = new TimeSlot(LocalTime.of(10, 0));

        assertThatThrownBy(() -> ReservationDateTime.forReserve(yesterday, timeSlot))
            .isInstanceOf(BusinessRuleViolationException.class);
    }
}

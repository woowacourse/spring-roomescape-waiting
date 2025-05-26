package roomescape.domain.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static roomescape.DateUtils.yesterday;
import static roomescape.TestFixtures.anyThemeWithNewId;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.RoomescapeSchedule;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.BusinessRuleViolationException;

class ReservationDateTimeTest {

    @Test
    @DisplayName("예약하려는 방탈출 일정의 일시가 현재 일시보다 이전이면 예외가 발생한다.")
    void isBefore() {
        var yesterday = yesterday();
        var timeSlot = new TimeSlot(LocalTime.of(10, 0));
        var theme = anyThemeWithNewId();

        assertThatThrownBy(() -> RoomescapeSchedule.forReserve(yesterday, timeSlot, theme))
            .isInstanceOf(BusinessRuleViolationException.class);
    }
}

package roomescape.domain.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidReservationException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ScheduleTest {
    @DisplayName("일정은 현재보다 이전일 수 없다.")
    @Test
    void invalidSchedule() {
        //given
        ReservationDate pastDate = ReservationDate.of(LocalDate.MIN);
        ReservationTime time = new ReservationTime(LocalTime.of(10,0));

        //when & then
        assertThatThrownBy(() -> new Schedule(pastDate, time))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("현재보다 이전으로 일정을 설정할 수 없습니다.");
    }
}

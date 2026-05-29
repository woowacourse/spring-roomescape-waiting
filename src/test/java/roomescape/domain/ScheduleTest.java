package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleTest {

    @Test
    @DisplayName("스케줄의 날짜와 시간이 현재 시간보다 이전인지 확인한다.")
    void isPastTrueTest() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 5);
        ReservationTime time = ReservationTime.from(1L, LocalTime.of(10, 0));
        Schedule schedule = Schedule.from(date, time);

        LocalDateTime now = LocalDateTime.of(2026, 6, 5, 10, 1);

        // when
        boolean result = schedule.isPast(now);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("스케줄의 날짜와 시간이 현재 시간과 같거나 미래라면 과거가 아님을 확인한다.")
    void isPastFalseTest() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 5);
        ReservationTime time = ReservationTime.from(1L, LocalTime.of(10, 0));
        Schedule schedule = Schedule.from(date, time);

        LocalDateTime now = LocalDateTime.of(2026, 6, 5, 10, 0);

        // when
        boolean result = schedule.isPast(now);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("날짜와 시간이 모두 동일한 스케줄은 같은 객체로 판단한다.")
    void equalsAndHashCodeTest() {
        // given
        LocalDate date = LocalDate.of(2026, 6, 5);
        ReservationTime time = ReservationTime.from(1L, LocalTime.of(10, 0));

        Schedule schedule1 = Schedule.from(date, time);
        Schedule schedule2 = Schedule.from(LocalDate.of(2026, 6, 5), ReservationTime.from(1L, LocalTime.of(10, 0)));

        // when & then
        assertThat(schedule1).isEqualTo(schedule2);
        assertThat(schedule1.hashCode()).isEqualTo(schedule2.hashCode());
    }
}

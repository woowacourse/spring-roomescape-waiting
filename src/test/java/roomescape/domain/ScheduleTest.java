package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static roomescape.support.TestDateTimes.FIXED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomEscapeException;
import roomescape.support.TestDateTimes;

class ScheduleTest {

    @Test
    void 날짜_정보가_없다면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(TestDateTimes.defaultTime());

        // when & then
        assertThatThrownBy(() -> Schedule.of(null, time))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("예약 날짜 및 시간 정보는 비어있을 수 없습니다.");
    }

    @Test
    void 시간_정보가_없다면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Schedule.of(FIXED.toLocalDate(), null))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("예약 날짜 및 시간 정보는 비어있을 수 없습니다.");
    }

    @Test
    void 유효한_스케줄을_생성할_수_있다() {
        // given
        LocalDate date = FIXED.toLocalDate();
        ReservationTime time = ReservationTime.create(FIXED.toLocalTime());

        // when
        Schedule schedule = Schedule.of(date, time);

        // then
        assertThat(schedule)
                .extracting(Schedule::getDate, Schedule::getStartAt)
                .containsExactly(date, FIXED.toLocalTime());
    }

    @Test
    void 날짜와_시간을_조합했을_때_과거라면_true를_반환한다() {
        // given
        LocalDateTime pastDateTime = FIXED.minusDays(1);
        Schedule schedule = Schedule.of(pastDateTime.toLocalDate(), ReservationTime.create(pastDateTime.toLocalTime()));

        // when
        boolean isPast = schedule.isPast(FIXED);

        // then
        assertThat(isPast).isTrue();
    }

    @Test
    void 날짜와_시간을_조합했을_때_미래라면_false를_반환한다() {
        // given
        LocalDateTime futureDateTime = FIXED.plusDays(1);
        Schedule schedule = Schedule.of(futureDateTime.toLocalDate(), ReservationTime.create(futureDateTime.toLocalTime()));

        // when
        boolean isPast = schedule.isPast(FIXED);

        // then
        assertThat(isPast).isFalse();
    }

    @Test
    void 날짜와_시간이_같으면_동등하다() {
        // given
        LocalDate date = TestDateTimes.today();
        ReservationTime time = ReservationTime.restore(1L, TestDateTimes.defaultTime(), TimeStatus.ACTIVE);

        // when
        Schedule first = Schedule.of(date, time);
        Schedule second = Schedule.of(date, ReservationTime.restore(1L, TestDateTimes.defaultTime(), TimeStatus.ACTIVE));

        // then
        assertThat(first).isEqualTo(second);
    }
}

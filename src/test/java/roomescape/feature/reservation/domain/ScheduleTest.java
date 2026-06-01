package roomescape.feature.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.feature.time.domain.Time;
import roomescape.fixture.TimeFixture;

class ScheduleTest {

    private static final Time DEFAULT_TIME = TimeFixture.VALID_10_00.createInstance();

    @Nested
    class 과거_여부를_판단한다 {

        @Test
        void 과거_날짜와_시간이면_true를_반환한다() {
            Schedule schedule = new Schedule(LocalDate.now().minusYears(1), DEFAULT_TIME);

            assertThat(schedule.isPast()).isTrue();
        }

        @Test
        void 미래_날짜와_시간이면_false를_반환한다() {
            Schedule schedule = new Schedule(LocalDate.now().plusYears(1), DEFAULT_TIME);

            assertThat(schedule.isPast()).isFalse();
        }
    }
}

package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;
import roomescape.time.Time;

class TimeTest {

    @Nested
    class Constructor {

        @Test
        @DisplayName("영업 시간 범위 내의 시간이면 생성에 성공한다")
        void createsWithValidTime() {
            Time time = new Time(1L, LocalTime.of(10, 0));

            assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("10시 이전이면 예외를 던진다")
        void throwsWhenTimeBefore10() {
            assertThatThrownBy(() -> new Time(1L, LocalTime.of(9, 59)))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("22시 이후이면 예외를 던진다")
        void throwsWhenTimeAfter22() {
            assertThatThrownBy(() -> new Time(1L, LocalTime.of(22, 1)))
                    .isInstanceOf(InvalidInputException.class);
        }
    }
}

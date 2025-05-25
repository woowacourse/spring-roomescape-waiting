package roomescape.business.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("startAt 필드에 null 들어오면 예외가 발생한다")
    void validateStartAt() {
        // given
        final LocalTime invalidStartAt = null;

        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> new ReservationTime(invalidStartAt))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> new ReservationTime(1L, invalidStartAt))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @Test
    @DisplayName("인자값보다 예약 시간대가 과거이면 true를 반환한다")
    void isPastWhenTimeIsAfterStartAt() {
        // given
        LocalTime reservationTime = LocalTime.of(14, 0);
        ReservationTime time = new ReservationTime(1L, reservationTime);

        // when
        LocalTime futureTime = LocalTime.of(16, 0);
        boolean result = time.isPast(futureTime);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("인자값보다 예약 시간대가 미래이면 false를 반환한다")
    void isPastWhenTimeIsBeforeStartAt() {
        // given
        LocalTime reservationTime = LocalTime.of(14, 0);
        ReservationTime time = new ReservationTime(1L, reservationTime);

        // when
        LocalTime pastTime = LocalTime.of(10, 0);
        boolean result = time.isPast(pastTime);

        // then
        assertThat(result).isFalse();
    }
}

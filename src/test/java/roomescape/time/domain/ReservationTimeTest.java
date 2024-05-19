package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {
    @DisplayName("시간이 비어있을 때 예외를 던진다.")
    @Test
    void validateTimeTest_whenTimeIsNull() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("시간을 통해 특정 시간대 이전임을 알 수 있다.")
    @Test
    void isAfterTest_whenTimeIsBefore() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(9, 0));
        LocalTime localTime = LocalTime.of(9, 1);
        assertThat(time.isBefore(localTime)).isTrue();
    }

    @DisplayName("시간을 통해 특정 시간대 이후임을 알 수 있다.")
    @Test
    void isAfterTest_whenTimeIsAfter() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(9, 0));
        LocalTime localTime = LocalTime.of(8, 59);
        assertThat(time.isBefore(localTime)).isFalse();
    }
}

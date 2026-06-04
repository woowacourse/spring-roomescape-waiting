package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.PastReservationException;

class ReservationTimeTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 5);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 5, 12, 0);

    @Test
    @DisplayName("now보다 이른 시각은 과거다.")
    void isPastTrue() {
        ReservationTime time = ReservationTime.create(1, LocalTime.of(10, 0));

        assertThat(time.isPast(DATE, NOW)).isTrue();
    }

    @Test
    @DisplayName("now보다 늦은 시각은 과거가 아니다.")
    void isPastFalse() {
        ReservationTime time = ReservationTime.create(1, LocalTime.of(14, 0));

        assertThat(time.isPast(DATE, NOW)).isFalse();
    }

    @Test
    @DisplayName("now와 정확히 같은 시각은 과거가 아니다(경계).")
    void isPastBoundary() {
        ReservationTime time = ReservationTime.create(1, LocalTime.of(12, 0));

        assertThat(time.isPast(DATE, NOW)).isFalse();
    }

    @Test
    @DisplayName("과거 시각은 검증 시 예외를 던진다.")
    void validateNotPastThrows() {
        ReservationTime time = ReservationTime.create(1, LocalTime.of(10, 0));

        assertThatThrownBy(() -> time.validateNotPast(DATE, NOW))
                .isInstanceOf(PastReservationException.class);
    }

    @Test
    @DisplayName("미래 시각은 검증을 통과한다.")
    void validateNotPastOk() {
        ReservationTime time = ReservationTime.create(1, LocalTime.of(14, 0));

        assertThatCode(() -> time.validateNotPast(DATE, NOW))
                .doesNotThrowAnyException();
    }
}

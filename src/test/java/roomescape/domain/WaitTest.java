package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;

public class WaitTest {

    @Test
    void validateDeletableThrowsWhenDateBeforeToday() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Wait wait = new Wait(1L, LocalDateTime.now(), "fizz", LocalDate.of(2026, 5, 1), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 9, 0);

        assertThatThrownBy(() -> wait.validateDeletable(now))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.PAST_RESERVATION_DELETE));
    }

    @Test
    void validateDeletableDoesNotThrowWhenDateAfterToday() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Wait wait = new Wait(1L, LocalDateTime.now(), "fizz", LocalDate.of(2026, 5, 3), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 11, 0);

        assertThatNoException().isThrownBy(() -> wait.validateDeletable(now));
    }

}
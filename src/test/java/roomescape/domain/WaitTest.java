package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

public class WaitTest {

    @Test
    void isPastWhenDateIsBeforeToday() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Wait wait = new Wait(1L, LocalDateTime.now(), "fizz", LocalDate.of(2026, 5, 1), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 9, 0);

        assertThat(wait.isPast(now)).isTrue();
    }

    @Test
    void isPastWhenDateIsAfterToday() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Wait wait = new Wait(1L, LocalDateTime.now(), "fizz", LocalDate.of(2026, 5, 3), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 11, 0);

        assertThat(wait.isPast(now)).isFalse();
    }

    @Test
    void isPastWhenSameDateAndTimePast() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Wait wait = new Wait(1L, LocalDateTime.now(), "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 11, 0);

        assertThat(wait.isPast(now)).isTrue();
    }

    @Test
    void isPastWhenSameDateAndTimeFuture() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Wait wait = new Wait(1L, LocalDateTime.now(), "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 9, 0);

        assertThat(wait.isPast(now)).isFalse();
    }
}
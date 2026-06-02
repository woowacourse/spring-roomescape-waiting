package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.domain.exception.DomainErrorCode;

public class ReservationTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void nameBlankExceptionTest(String name) {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Reservation(1L, name, LocalDate.of(2026, 5, 2), reservationTime, theme))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.INVALID_INPUT));
    }

    @Test
    void dateNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Reservation(1L, "fizz", null, reservationTime, theme))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.INVALID_INPUT));
    }

    @Test
    void reservationTimeNullExceptionTest() {
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), null, theme))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.INVALID_INPUT));
    }

    @Test
    void themeNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        assertThatThrownBy(() -> new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, null))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.INVALID_INPUT));
    }

    @Test
    void isPastWhenDateIsBeforeToday() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Reservation reservation = new Reservation(1L, "fizz", LocalDate.of(2026, 5, 1), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 9, 0);

        assertThat(reservation.isPast(now)).isTrue();
    }

    @Test
    void isPastWhenDateIsAfterToday() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Reservation reservation = new Reservation(1L, "fizz", LocalDate.of(2026, 5, 3), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 11, 0);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    void isPastWhenSameDateAndTimePast() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Reservation reservation = new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 11, 0);

        assertThat(reservation.isPast(now)).isTrue();
    }

    @Test
    void isPastWhenSameDateAndTimeFuture() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        Reservation reservation = new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 9, 0);

        assertThat(reservation.isPast(now)).isFalse();
    }
}

package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.CustomInvalidDomainException;
import roomescape.exception.ErrorCode;

public class ReservationTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void nameBlankExceptionTest(String name) {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Reservation(1L, name, LocalDate.of(2026, 5, 2), reservationTime, theme))
                .isInstanceOf(CustomInvalidDomainException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_NAME_NULL.getMessage());
    }

    @Test
    void dateNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Reservation(1L, "fizz", null, reservationTime, theme))
                .isInstanceOf(CustomInvalidDomainException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_DATE_NULL.getMessage());
    }

    @Test
    void reservationTimeNullExceptionTest() {
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), null, theme))
                .isInstanceOf(CustomInvalidDomainException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_TIME_NULL.getMessage());
    }

    @Test
    void themeNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        assertThatThrownBy(() -> new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, null))
                .isInstanceOf(CustomInvalidDomainException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_THEME_NULL.getMessage());
    }

    @Test
    void checkPastTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "방탈출1", "방탈출1 설명", "url.jpg");
        Reservation pastReservation = new Reservation(1L, "fizz", LocalDate.of(2025, 5, 2), reservationTime, theme);
        Reservation futureReservation = new Reservation(1L, "fizz", LocalDate.of(3025, 5, 2), reservationTime, theme);

        LocalDateTime localDateTime = LocalDateTime.of(2026, 5, 13, 10, 0);

        assertThat(pastReservation.isPast(localDateTime)).isTrue();
        assertThat(futureReservation.isPast(localDateTime)).isFalse();
    }
}

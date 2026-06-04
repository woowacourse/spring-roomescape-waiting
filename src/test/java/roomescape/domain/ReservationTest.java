package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

class ReservationTest {

    @DisplayName("예약 정상 생성")
    @Test
    void createReservation_Success() {
        // given
        String name = "쿠다";
        LocalDate date = LocalDate.parse("2026-08-06");
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.createNew(LocalTime.parse("10:00"));

        // when & then
        assertThatCode(() -> Reservation.createNew(name, date, theme, time))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약자 이름 null, 공백 예외")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void validateName_ThrowsException(String name) {
        // given
        LocalDate date = LocalDate.parse("2026-03-08");
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.createNew(LocalTime.parse("10:00"));

        // when & then
        assertThatThrownBy(() -> Reservation.createNew(name, date, theme, time))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 날짜 null 예외")
    @Test
    void validateDate_ThrowsException() {
        // given
        String name = "쿠다";
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.createNew(LocalTime.parse("10:00"));

        // when & then
        assertThatThrownBy(() -> Reservation.createNew(name, null, theme, time))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 시간 null 예외")
    @Test
    void validateTime_ThrowsException() {
        // given
        String name = "쿠다";
        LocalDate date = LocalDate.parse("2026-03-08");
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");

        // when & then
        assertThatThrownBy(() -> Reservation.createNew(name, date, theme, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("과거 날짜와 시간으로는 예약할 수 없다")
    @Test
    void createPastDateTime_ThrowsException() {
        // given
        String name = "쿠다";
        LocalDate date = LocalDate.parse("2026-03-08");
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.createNew(LocalTime.parse("10:00"));
        LocalDateTime standardDateTime = LocalDateTime.parse("2026-03-08T10:01:00");

        // when & then
        assertThatThrownBy(() -> Reservation.createNew(name, date, theme, time, standardDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Reservation.PAST_RESERVATION_MESSAGE);
    }

    @DisplayName("예약 시각이 기준 시각보다 이전이면 과거 예약이다")
    @Test
    void isPastReservation() {
        // given
        LocalDate date = LocalDate.parse("2026-03-08");
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.createNew(LocalTime.parse("10:00"));
        Reservation reservation = Reservation.of(1L, "쿠다", date, theme, time);

        // when
        boolean past = reservation.isPast(LocalDateTime.parse("2026-03-08T10:01:00"));

        // then
        assertThat(past).isTrue();
    }
}

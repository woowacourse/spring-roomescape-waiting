package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationTest {
    private final Member member = new Member(1L, "커찬", "kuchn@abc.com");
    private final LocalDate date = LocalDate.of(2050, 10, 10);
    private final ReservationTime time = new ReservationTime(LocalTime.of(9, 0));
    private final Theme theme = new Theme(
            "오리와 호랑이",
            "오리들과 호랑이들 사이에서 살아남기",
            "https://image.jpg");

    @DisplayName("예약자가 비어있을 때 예외를 던진다.")
    @Test
    void validateReservationTest_whenNameIsNull() {
        assertThatThrownBy(() ->
                new Reservation(1L, null, date, time, theme))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("날짜 비어있을 때 예외를 던진다.")
    @Test
    void validateReservationTest_whenDateIsNull() {
        assertThatThrownBy(() ->
                new Reservation(1L, member, null, time, theme))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("시간 비어있을 때 예외를 던진다.")
    @Test
    void validateReservationTest_whenTimeIsNull() {
        assertThatThrownBy(() ->
                new Reservation(1L, member, date, null, theme))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("테마 비어있을 때 예외를 던진다.")
    @Test
    void validateReservationTest_whenThemeIsNull() {
        assertThatThrownBy(() ->
                new Reservation(1L, member, date, time, null))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("날짜를 통해 특정 시간대 이전임을 알 수 있다.")
    @Test
    void isAfterTest_whenDateIsBefore() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(9, 0));
        Reservation reservation = new Reservation(1L, member, LocalDate.of(2024, 4, 30), time, theme);
        LocalDateTime currentDateTime = LocalDateTime.of(2024, 5, 1, 10, 0);

        assertThat(reservation.isBefore(currentDateTime)).isTrue();
    }

    @DisplayName("날짜를 통해 특정 시간대 이후임을 알 수 있다.")
    @Test
    void isAfterTest_whenDateIsAfter() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(9, 0));
        Reservation reservation = new Reservation(1L, member, LocalDate.of(2024, 4, 30), time, theme);
        LocalDateTime currentDateTime = LocalDateTime.of(2024, 4, 29, 10, 0);

        assertThat(reservation.isBefore(currentDateTime)).isFalse();
    }

    @DisplayName("날짜가 같은 경우, 시간을 통해 판단한다.")
    @Test
    void isAfterTest_whenDateIsEqualTimeIsBefore() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(9, 0));
        Reservation reservation = new Reservation(1L, member, LocalDate.of(2024, 4, 30), time, theme);
        LocalDateTime currentDateTime = LocalDateTime.of(2024, 4, 30, 10, 0);

        assertThat(reservation.isBefore(currentDateTime)).isTrue();
    }
}

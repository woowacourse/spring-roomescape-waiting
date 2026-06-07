package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

    @Test
    @DisplayName("예약자 이름이 비어있으면 도메인 예외가 발생한다.")
    void create_fail_whenNameIsBlank() {
        assertInvalidRequestException(
                () -> Reservation.create(
                        " ",
                        LocalDate.of(2026, 5, 15),
                        time,
                        theme,
                        LocalDateTime.of(2026, 5, 15, 9, 0)
                )
        );
    }

    @Test
    @DisplayName("예약 날짜가 null이면 도메인 예외가 발생한다.")
    void create_fail_whenDateIsNull() {
        assertInvalidRequestException(
                () -> Reservation.create(
                        "브라운",
                        null,
                        time,
                        theme,
                        LocalDateTime.of(2026, 5, 15, 9, 0)
                )
        );
    }

    @Test
    @DisplayName("예약 시간이 null이면 도메인 예외가 발생한다.")
    void create_fail_whenTimeIsNull() {
        assertInvalidRequestException(
                () -> Reservation.create(
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        null,
                        theme,
                        LocalDateTime.of(2026, 5, 15, 9, 0)
                )
        );
    }

    @Test
    @DisplayName("예약 테마가 null이면 도메인 예외가 발생한다.")
    void create_fail_whenThemeIsNull() {
        assertInvalidRequestException(
                () -> Reservation.create(
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        time,
                        null,
                        LocalDateTime.of(2026, 5, 15, 9, 0)
                )
        );
    }

    @Test
    @DisplayName("예약 id가 null이면 도메인 예외가 발생한다.")
    void withId_fail_whenIdIsNull() {
        Reservation reservation = Reservation.create(
                "브라운",
                LocalDate.of(2026, 5, 15),
                time,
                theme,
                LocalDateTime.of(2026, 5, 15, 9, 0)
        );

        assertInvalidRequestException(
                () -> reservation.withId(null)
        );
    }

    @Test
    @DisplayName("이미 id가 있는 예약에 id를 부여하면 도메인 예외가 발생한다.")
    void withId_fail_whenReservationAlreadyHasId() {
        Reservation reservation = Reservation.create(
                "브라운",
                LocalDate.of(2026, 5, 15),
                time,
                theme,
                LocalDateTime.of(2026, 5, 15, 9, 0)
        ).withId(1L);

        assertInvalidRequestException(
                () -> reservation.withId(2L)
        );
    }

    @Test
    @DisplayName("과거 날짜와 시간으로 예약을 생성하면 예외가 발생한다.")
    void create_fail_whenReservationDateTimeIsBeforeNow() {
        LocalDate date = LocalDate.of(2026, 5, 15);
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 11, 0);

        assertInvalidRequestException(
                () -> Reservation.create("브라운", date, time, theme, now)
        );
    }

    @Test
    @DisplayName("예약 날짜와 시간이 기준 시각보다 이후이면 예약을 생성한다.")
    void create_success_whenReservationDateTimeIsAfterNow() {
        LocalDate date = LocalDate.of(2026, 5, 15);
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 9, 0);

        Reservation reservation = Reservation.create("브라운", date, time, theme, now);

        assertThat(reservation.getDate()).isEqualTo(date);
        assertThat(reservation.getTime()).isEqualTo(time);
    }

    @Test
    @DisplayName("예약 날짜와 시간이 기준 시각보다 이전이면 과거 예약이다.")
    void isPastAt_success_whenReservationDateTimeIsBeforeNow() {
        LocalDate date = LocalDate.of(2026, 5, 15);
        Reservation reservation = Reservation.create(
                "브라운",
                date,
                time,
                theme,
                LocalDateTime.of(2026, 5, 15, 9, 0)
        );
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 11, 0);

        assertThat(reservation.isPast(now)).isTrue();
    }

    @Test
    @DisplayName("예약 날짜와 시간이 기준 시각과 같으면 과거 예약이 아니다.")
    void isPastAt_false_whenReservationDateTimeIsSameAsNow() {
        LocalDate date = LocalDate.of(2026, 5, 15);
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 10, 0);
        Reservation reservation = Reservation.create("브라운", date, time, theme, now);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    @DisplayName("예약 날짜와 시간이 기준 시각보다 이후이면 과거 예약이 아니다.")
    void isPastAt_false_whenReservationDateTimeIsAfterNow() {
        LocalDate date = LocalDate.of(2026, 5, 15);
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 9, 0);
        Reservation reservation = Reservation.create("브라운", date, time, theme, now);

        assertThat(reservation.isPast(now)).isFalse();
    }

    private void assertInvalidRequestException(Runnable runnable) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(InvalidRequestException.class);
    }
}

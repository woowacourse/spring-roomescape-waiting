package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.common.validate.InvalidInputException;
import roomescape.reservation.exception.PastDateReservationException;
import roomescape.reservation.exception.PastTimeReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.user.domain.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ReservationTest {

    @Test
    @DisplayName("유저 ID가 null이면 예외가 발생한다")
    void throwExceptionWhenUserIdIsNull() {
        // given
        ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        ReservationTime time = createReservationTime();
        Theme theme = createTheme();

        // when & then
        assertThatThrownBy(() -> Reservation.withoutId(null, date, time, theme))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: Reservation.userId");
    }

    @Test
    @DisplayName("예약 날짜가 null이면 예외가 발생한다")
    void throwExceptionWhenReservationDateIsNull() {
        // given
        UserId userId = UserId.from(1L);
        ReservationTime time = createReservationTime();
        Theme theme = createTheme();

        // when & then
        assertThatThrownBy(() -> Reservation.withoutId(userId, null, time, theme))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: Reservation.date");
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다")
    void throwExceptionWhenReservationTimeIsNull() {
        // given
        UserId userId = UserId.from(1L);
        ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        Theme theme = createTheme();

        // when & then
        assertThatThrownBy(() -> Reservation.withoutId(userId, date, null, theme))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: Reservation.time");
    }

    @Test
    @DisplayName("테마가 null이면 예외가 발생한다")
    void throwExceptionWhenThemeIsNull() {
        // given
        UserId userId = UserId.from(1L);
        ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        ReservationTime time = createReservationTime();

        // when & then
        assertThatThrownBy(() -> Reservation.withoutId(userId, date, time, null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: Reservation.theme");
    }

    @Test
    @DisplayName("예약 ID가 null이면 예외가 발생한다")
    void throwExceptionWhenReservationIdIsNull() {
        // given
        UserId userId = UserId.from(1L);
        ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        ReservationTime time = createReservationTime();
        Theme theme = createTheme();

        // when & then
        assertThatThrownBy(() -> Reservation.withId(null, userId, date, time, theme))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Validation failed [while checking null]: Reservation.id");
    }

    @Test
    @DisplayName("과거 날짜로 예약하면 예외가 발생한다")
    void throwExceptionWhenReservationDateIsInThePast() {
        // given
        UserId userId = UserId.from(1L);
        ReservationDate pastDate = ReservationDate.from(LocalDate.now().minusDays(1));
        ReservationTime time = createReservationTime();
        Theme theme = createTheme();
        Reservation reservation = Reservation.withoutId(userId, pastDate, time, theme);
        LocalDateTime now = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> reservation.validatePast(now))
                .isInstanceOf(PastDateReservationException.class);
    }

    @Test
    @DisplayName("같은 날짜의 과거 시간으로 예약하면 예외가 발생한다")
    void throwExceptionWhenReservationTimeIsInThePast() {
        // given
        UserId userId = UserId.from(1L);
        ReservationDate todayDate = ReservationDate.from(LocalDate.now());
        LocalDateTime now = LocalDateTime.now();
        ReservationTime pastTime = ReservationTime.withoutId(now.toLocalTime().minusHours(1));
        Theme theme = createTheme();
        Reservation reservation = Reservation.withoutId(userId, todayDate, pastTime, theme);

        // when & then
        assertThatThrownBy(() -> reservation.validatePast(now))
                .isInstanceOf(PastTimeReservationException.class);
    }

    private Theme createTheme() {
        return Theme.withoutId(
                ThemeName.from("테스트 테마"),
                ThemeDescription.from("테스트 설명"),
                ThemeThumbnail.from("test.jpg")
        );
    }

    private ReservationTime createReservationTime() {
        return ReservationTime.withoutId((LocalTime.of(14, 0)));
    }

}

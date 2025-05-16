package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.validate.InvalidInputException;
import roomescape.reservation.exception.PastDateReservationException;
import roomescape.reservation.exception.PastTimeReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationTest {

    @Test
    @DisplayName("예약 필드가 null이 될 수 없다")
    void validateNull() {
        // given
        final UserId userId = UserId.from(1L);
        final ReservationDate date = ReservationDate.from(LocalDate.now());
        final ReservationTime time = ReservationTime.withId(ReservationTimeId.from(1L), LocalTime.of(10, 0));
        final Theme theme = Theme.withId(
                ThemeId.from(1L),
                ThemeName.from("테마 이름"),
                ThemeDescription.from("테마 설명"),
                ThemeThumbnail.from("https://example.com/image.jpg"));
        final ReservationId id = ReservationId.from(1L);

        // when
        // then
        assertAll(
                () -> assertThatThrownBy(() -> Reservation.withId(null, userId, date, time, theme))
                        .isInstanceOf(NullPointerException.class),

                () -> assertThatThrownBy(() -> Reservation.withoutId(null, date, time, theme))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: Reservation.userId"),

                () -> assertThatThrownBy(() -> Reservation.withoutId(userId, null, time, theme))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: Reservation.date"),

                () -> assertThatThrownBy(() -> Reservation.withoutId(userId, date, null, theme))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: Reservation.time"),

                () -> assertThatThrownBy(() -> Reservation.withoutId(userId, date, time, null))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: Reservation.theme")
        );
    }

    @Test
    @DisplayName("유효한 값으로 Reservation 객체를 생성할 수 있다")
    void createValidReservation() {
        // given
        final UserId userId = UserId.from(1L);
        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        final ReservationTime time = ReservationTime.withId(ReservationTimeId.from(1L), LocalTime.of(10, 0));
        final Theme theme = Theme.withId(
                ThemeId.from(1L),
                ThemeName.from("테마 이름"),
                ThemeDescription.from("테마 설명"),
                ThemeThumbnail.from("https://example.com/image.jpg"));

        // when
        final Reservation reservation = Reservation.withoutId(userId, date, time, theme);

        // then
        assertAll(() -> {
            assertThat(reservation).isNotNull();
            assertThat(reservation.getUserId()).isEqualTo(userId);
            assertThat(reservation.getDate()).isEqualTo(date);
            assertThat(reservation.getTime()).isEqualTo(time);
            assertThat(reservation.getTheme()).isEqualTo(theme);
            assertThat(reservation.getStatus().isBooked()).isTrue();
        });
    }

    @Test
    @DisplayName("과거 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void validatePast() {
        final Theme savedTheme = Theme.withId(
                ThemeId.from(1234L),
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com"));

        final User savedUser = User.withId(
                UserId.from(1234L),
                UserName.from("강산"),
                Email.from("email@email.com"),
                Password.fromEncoded("1234"),
                UserRole.NORMAL);

        final LocalDateTime now = LocalDateTime.now();
        final LocalDate nowDate = now.toLocalDate();
        final LocalTime nowTime = now.toLocalTime();

        final Reservation minusDay = Reservation.withoutId(
                savedUser.getId(),
                ReservationDate.from(nowDate.minusDays(1L)),
                ReservationTime.withoutId(nowTime),
                savedTheme);

        final Reservation minusTime = Reservation.withoutId(
                savedUser.getId(),
                ReservationDate.from(nowDate),
                ReservationTime.withoutId(nowTime.minusMinutes(1L)),
                savedTheme);

        assertAll(() -> {

            assertThatThrownBy(() -> minusDay.validatePast(now))
                    .isInstanceOf(PastDateReservationException.class);

            assertThatThrownBy(() -> minusTime.validatePast(now))
                    .isInstanceOf(PastTimeReservationException.class);

        });
    }
}

package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.reservation.exception.PastDateReservationException;
import roomescape.reservation.exception.PastTimeReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationTest {

    @Test
    @DisplayName("과거 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void validatePast() {
        final Theme theme = Theme.withoutId(
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com"));

        final User user = User.withoutId(
                UserName.from("강산"),
                Email.from("email@email.com"),
                Password.fromEncoded("1234"),
                UserRole.NORMAL);

        final LocalDateTime now = LocalDateTime.now();
        final LocalDate nowDate = now.toLocalDate();
        final LocalTime nowTime = now.toLocalTime();

        final Reservation minusDay = Reservation.withoutId(
                user.getId(),
                ReservationDate.from(nowDate.minusDays(1L)),
                ReservationTime.withoutId(nowTime),
                theme);

        final Reservation minusTime = Reservation.withoutId(
                user.getId(),
                ReservationDate.from(nowDate),
                ReservationTime.withoutId(nowTime.minusMinutes(1L)),
                theme);

        assertAll(() -> {

            assertThatThrownBy(() -> minusDay.validatePast(now))
                    .isInstanceOf(PastDateReservationException.class);

            assertThatThrownBy(() -> minusTime.validatePast(now))
                    .isInstanceOf(PastTimeReservationException.class);

        });
    }
}

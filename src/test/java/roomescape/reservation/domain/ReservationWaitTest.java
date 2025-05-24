package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;

class ReservationWaitTest {

    @DisplayName("예약 대기를 예약 객체로 변경한다.")
    @Test
    void toReservation() {
        // given
        final Member member = Member.withoutId(
                MemberName.from("Test User"),
                MemberEmail.from("test@example.com"),
                Role.MEMBER
        );
        final ReservationDate date = ReservationDate.from(LocalDate.of(9999, 12, 31));
        final ReservationTime time = ReservationTime.withoutId(LocalTime.of(23, 59));
        final Theme theme = Theme.withoutId(
                ThemeName.from("Escape Room 1"),
                ThemeDescription.from("An exciting escape room theme."),
                ThemeThumbnail.from("http://example.com/thumbnail.jpg")
        );
        final ReservationWait reservationWait = ReservationWait.withId(1L, member, date, time, theme);

        // when
        final Reservation reservation = reservationWait.toReservation();

        // then
        assertAll(
                () -> assertThat(reservation).isNotNull(),
                () -> assertThat(reservation.getMember()).isEqualTo(member),
                () -> assertThat(reservation.getDate()).isEqualTo(date),
                () -> assertThat(reservation.getTime()).isEqualTo(time),
                () -> assertThat(reservation.getTheme()).isEqualTo(theme)
        );
    }
}

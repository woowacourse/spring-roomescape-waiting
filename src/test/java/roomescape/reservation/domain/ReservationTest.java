package roomescape.reservation.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;

class ReservationTest {

    @Test
    @DisplayName("과거 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void validatePast() {
        final Theme theme = Theme.withoutId(ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com"));

        final Member member = Member.withoutId(MemberName.from("강산"),
                MemberEmail.from("123@gmail.com"),
                Role.MEMBER);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThatThrownBy(() -> Reservation.withoutId(
                    member,
                    ReservationDate.from(LocalDate.now().minusDays(1L)),
                    ReservationTime.withoutId(LocalTime.now()),
                    theme
            )).isInstanceOf(BadRequestException.class);
            softAssertions.assertThatThrownBy(() -> Reservation.withoutId(
                    member,
                    ReservationDate.from(LocalDate.now()),
                    ReservationTime.withoutId(LocalTime.now().minusMinutes(1L)),
                    theme
            )).isInstanceOf(BadRequestException.class);
        });
    }
}

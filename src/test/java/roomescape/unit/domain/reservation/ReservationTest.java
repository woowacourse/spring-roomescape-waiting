package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;

class ReservationTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(
            1L,
            new ThemeName("공포"),
            new ThemeDescription("무섭다"),
            new ThemeThumbnail("thumb.jpg")
    );

    @Test
    void reserverName은_null일_수_없다() {
        // when // then
        assertThatThrownBy(() ->
                new Reservation(1L, null, new ReservationDate(LocalDate.now()), time, theme)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void reservationDate는_null일_수_없다() {
        // given
        var member = new Member(
                1L,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("dsadsa"),
                MemberRole.MEMBER
        );

        // when // then
        assertThatThrownBy(() ->
                new Reservation(1L, member, null, time, theme)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void reservationTime은_null일_수_없다() {
        // given
        var member = new Member(
                1L,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("dsadsa"),
                MemberRole.MEMBER
        );

        // when // then
        assertThatThrownBy(() ->
                new Reservation(1L, member, new ReservationDate(LocalDate.now()), null, theme)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void theme은_null일_수_없다() {
        // given
        var member = new Member(
                1L,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("dsadsa"),
                MemberRole.MEMBER
        );

        // when // then
        assertThatThrownBy(() ->
                new Reservation(1L, member, new ReservationDate(LocalDate.now()), time, null)
        ).isInstanceOf(NullPointerException.class);
    }
}

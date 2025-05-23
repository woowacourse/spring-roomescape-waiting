package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.ReservationWait;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.fixture.ReservationDateFixture;

class ReservationWaitTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(
            1L,
            new ThemeName("공포"),
            new ThemeDescription("무섭다"),
            new ThemeThumbnail("thumb.jpg")
    );

    @Test
    void 멤버는_null일_수_없다() {
        Member member = new Member(
                1L,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("dsadsa"),
                MemberRole.MEMBER
        );
        ReservationSchedule schedule = new ReservationSchedule(1L, ReservationDateFixture.예약날짜_오늘, time, theme);
        assertThatThrownBy(() -> new ReservationWait(1L, null, schedule))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 예약_일정은_null일_수_없다() {
        Member member = new Member(
                1L,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("dsadsa"),
                MemberRole.MEMBER
        );
        assertThatThrownBy(() -> new ReservationWait(1L, member, null))
                .isInstanceOf(NullPointerException.class);
    }

}

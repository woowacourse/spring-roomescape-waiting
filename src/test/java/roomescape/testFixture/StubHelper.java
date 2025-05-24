package roomescape.testFixture;

import java.time.LocalTime;
import org.mockito.Mockito;
import roomescape.application.MemberService;
import roomescape.application.ThemeService;
import roomescape.application.TimeService;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

public class StubHelper {

    public static Member stubMember(long memberId, MemberService memberService) {
        Member member = Member.of(memberId, "브라운", "brown@email.com", "brown", Role.USER);
        Mockito.doReturn(member).when(memberService).getMemberEntityById(memberId);
        return member;
    }

    public static Theme stubTheme(long themeId, ThemeService themeService) {
        Theme theme = Theme.of(themeId, "테마1", "테마1입니다.", "썸네일1");
        Mockito.doReturn(theme).when(themeService).getThemeById(themeId);
        return theme;
    }

    public static ReservationTime stubTime(long timeId, TimeService timeService) {
        ReservationTime time = ReservationTime.of(timeId, LocalTime.of(10, 0));
        Mockito.doReturn(time).when(timeService).getTimeEntityById(timeId);
        return time;
    }
}

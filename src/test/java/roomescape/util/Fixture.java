package roomescape.util;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.auth.domain.Role;
import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

public class Fixture {

    public static final Theme HORROR_THEME = new Theme(
            new ThemeName("공포"),
            new Description("무서운 테마 입니다."),
            "https://i.pinimg.com/236x.jpg"
    );

    public static final Theme ACTION_THEME = new Theme(
            new ThemeName("액션"),
            new Description("액션 테마 입니다."),
            "https://i.pinimg.com/236x.jpg"
    );

    public static final LocalTime HOUR_10 = LocalTime.parse("10:00");
    public static final LocalTime HOUR_11 = LocalTime.parse("11:00");
    public static final ReservationTime RESERVATION_HOUR_10 = new ReservationTime(HOUR_10);
    public static final ReservationTime RESERVATION_HOUR_11 = new ReservationTime(HOUR_11);

    public static final String KAKI_NAME = "카키";
    public static final String KAKI_EMAIL = "kaki@email.com";
    public static final String KAKI_PASSWORD = "1234";

    public static final String JOJO_NAME = "조조";
    public static final String JOJO_EMAIL = "jojo@email.com";
    public static final String JOJO_PASSWORD = "1234";

    public static final Member KAKI = Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD);
    public static final Member JOJO = Member.createMemberByUserRole(new MemberName(JOJO_NAME), JOJO_EMAIL, JOJO_PASSWORD);

    public static final LoginMember LOGIN_MEMBER_KAKI = new LoginMember(1L, Role.USER, KAKI_NAME, KAKI_EMAIL);
    public static final LoginMember LOGIN_MEMBER_JOJO = new LoginMember(2L, Role.USER, JOJO_NAME, JOJO_EMAIL);

    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    private Fixture() {
    }
}

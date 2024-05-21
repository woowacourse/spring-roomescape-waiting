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

    public static final String HORROR_THEME_NAME = "공포";
    public static final String HORROR_DESCRIPTION = "공포";
    public static final String THUMBNAIL = "https://i.pinimg.com/236x.jpg";

    public static final Theme HORROR_THEME = new Theme(
            new ThemeName(HORROR_THEME_NAME),
            new Description(HORROR_DESCRIPTION),
            THUMBNAIL
    );

    public static final String HOUR_10 = "10:00";

    public static final ReservationTime RESERVATION_TIME_10_00 = new ReservationTime(LocalTime.parse(HOUR_10));

    public static final String KAKI_NAME = "카키";
    public static final String KAKI_EMAIL = "kaki@email.com";
    public static final String KAKI_PASSWORD = "1234";

    public static final String JOJO_NAME = "조조";
    public static final String JOJO_EMAIL = "jojo@email.com";
    public static final String JOJO_PASSWORD = "1234";

    public static final Member MEMBER_KAKI = new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD);
    public static final Member MEMBER_JOJO = new Member(new MemberName(JOJO_NAME), JOJO_EMAIL, JOJO_PASSWORD);
    public static final LoginMember LOGIN_JOJO = new LoginMember(1L, Role.MEMBER, JOJO_NAME, JOJO_EMAIL);

    public static final LocalDate TODAY = LocalDate.now();

    private Fixture() {
    }
}

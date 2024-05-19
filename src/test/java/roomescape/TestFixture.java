package roomescape;

import roomescape.domain.member.Member;
import roomescape.domain.member.Name;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

public class TestFixture {

    public static final String ADMIN_NAME = "냥인";
    public static final String ADMIN_EMAIL = "nyangin@email.com";
    public static final String ADMIN_PASSWORD = "1234";
    public static final String MEMBER_MIA_NAME = "미아";
    public static final String MEMBER_MIA_EMAIL = "mia@email.com";
    public static final String MEMBER_BROWN_NAME = "브라운";
    public static final String MEMBER_BROWN_EMAIL = "brown@email.com";
    public static final String MEMBER_PASSWORD = "1234";

    public static final String DATE_MAY_EIGHTH = "2034-05-08";
    public static final String DATE_MAY_NINTH = "2034-05-09";

    public static final String START_AT_SIX = "18:00";
    public static final String START_AT_SEVEN = "19:00";

    public static final String THEME_HORROR_NAME = "호러";
    public static final String THEME_HORROR_DESCRIPTION = "매우 무섭습니다.";
    public static final String THEME_HORROR_THUMBNAIL = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";

    public static final String THEME_DETECTIVE_NAME = "추리";
    public static final String THEME_DETECTIVE_DESCRIPTION = "매우 어렵습니다.";
    public static final String THEME_DETECTIVE_THUMBNAIL = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";

    public static Member ADMIN() {
        return new Member(new Name(ADMIN_NAME), ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
    }

    public static Member ADMIN(final Long id) {
        return new Member(id, new Name(ADMIN_NAME), ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
    }

    public static Member MEMBER_MIA() {
        return new Member(new Name(MEMBER_MIA_NAME), MEMBER_MIA_EMAIL, MEMBER_PASSWORD, Role.MEMBER);
    }

    public static Member MEMBER_MIA(final Long id) {
        return new Member(id, new Name(MEMBER_MIA_NAME), MEMBER_MIA_EMAIL, MEMBER_PASSWORD, Role.MEMBER);
    }

    public static Member MEMBER_BROWN() {
        return new Member(new Name(MEMBER_BROWN_NAME), MEMBER_BROWN_EMAIL, MEMBER_PASSWORD, Role.MEMBER);
    }

    public static ReservationTime RESERVATION_TIME_SIX() {
        return new ReservationTime(START_AT_SIX);
    }

    public static ReservationTime RESERVATION_TIME_SIX(final Long id) {
        return new ReservationTime(id, START_AT_SIX);
    }

    public static ReservationTime RESERVATION_TIME_SEVEN() {
        return new ReservationTime(START_AT_SEVEN);
    }

    public static ReservationTime RESERVATION_TIME_SEVEN(final Long id) {
        return new ReservationTime(id, START_AT_SEVEN);
    }

    public static Theme THEME_HORROR() {
        return new Theme(THEME_HORROR_NAME, THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
    }

    public static Theme THEME_HORROR(final Long id) {
        return new Theme(id, THEME_HORROR_NAME, THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
    }

    public static Theme THEME_DETECTIVE() {
        return new Theme(THEME_DETECTIVE_NAME, THEME_DETECTIVE_DESCRIPTION, THEME_DETECTIVE_THUMBNAIL);
    }

    public static Theme THEME_DETECTIVE(final Long id) {
        return new Theme(id, THEME_DETECTIVE_NAME, THEME_DETECTIVE_DESCRIPTION, THEME_DETECTIVE_THUMBNAIL);
    }
}

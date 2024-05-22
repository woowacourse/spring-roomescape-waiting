package roomescape;

import roomescape.domain.member.Member;
import roomescape.domain.member.Name;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;

public class TestFixture {

    public static final String ADMIN_NAME = "테니";
    public static final String ADMIN_EMAIL = "tenny@email.com";
    public static final String ADMIN_PASSWORD = "1234";
    public static final String MEMBER_CAT_NAME = "냥인";
    public static final String MEMBER_CAT_EMAIL = "nyangin@email.com";
    public static final String MEMBER_BROWN_NAME = "브라운";
    public static final String MEMBER_BROWN_EMAIL = "brown@email.com";
    public static final String MEMBER_PASSWORD = "1234";

    public static final LocalDate DATE_MAY_TWENTY = LocalDate.of(2024, 5, 20);
    public static final LocalDate DATE_MAY_ONE = LocalDate.of(2034, 5, 1);
    public static final LocalDate DATE_MAY_EIGHTH = LocalDate.of(2034, 5, 8);
    public static final LocalDate DATE_MAY_NINTH = LocalDate.of(2034, 5, 9);

    public static final String START_AT_ONE = "13:00";
    public static final String START_AT_SIX = "18:00";
    public static final String START_AT_SEVEN = "19:00";

    public static final String THEME_COMIC_NAME = "커비";
    public static final String THEME_COMIC_DESCRIPTION = "매우 재밌습니다.";
    public static final String THEME_COMIC_THUMBNAIL = "https://i.pinimg.com/474x/e2/55/4d/e2554dea5499f88c242178ce7ed568d9.jpg";

    public static final String THEME_ANIME_NAME = "포켓몬";
    public static final String THEME_ANIME_DESCRIPTION = "매우 귀엽습니다.";
    public static final String THEME_ANIME_THUMBNAIL = "https://i.pinimg.com/474x/b3/aa/d7/b3aad752a5fbda932dd37015bca3047f.jpg";

    public static Member ADMIN() {
        return new Member(new Name(ADMIN_NAME), ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
    }

    public static Member ADMIN(final Long id) {
        return new Member(id, new Name(ADMIN_NAME), ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
    }

    public static Member MEMBER_CAT() {
        return new Member(new Name(MEMBER_CAT_NAME), MEMBER_CAT_EMAIL, MEMBER_PASSWORD, Role.MEMBER);
    }

    public static Member MEMBER_CAT(final Long id) {
        return new Member(id, new Name(MEMBER_CAT_NAME), MEMBER_CAT_EMAIL, MEMBER_PASSWORD, Role.MEMBER);
    }

    public static Member MEMBER_BROWN() {
        return new Member(new Name(MEMBER_BROWN_NAME), MEMBER_BROWN_EMAIL, MEMBER_PASSWORD, Role.MEMBER);
    }

    public static ReservationTime RESERVATION_TIME_ONE() {
        return new ReservationTime(START_AT_ONE);
    }

    public static ReservationTime RESERVATION_TIME_ONE(final Long id) {
        return new ReservationTime(id, START_AT_ONE);
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

    public static Theme THEME_COMIC() {
        return new Theme(THEME_COMIC_NAME, THEME_COMIC_DESCRIPTION, THEME_COMIC_THUMBNAIL);
    }

    public static Theme THEME_COMIC(final Long id) {
        return new Theme(id, THEME_COMIC_NAME, THEME_COMIC_DESCRIPTION, THEME_COMIC_THUMBNAIL);
    }

    public static Theme THEME_ANIME() {
        return new Theme(THEME_ANIME_NAME, THEME_ANIME_DESCRIPTION, THEME_ANIME_THUMBNAIL);
    }

    public static Theme THEME_ANIME(final Long id) {
        return new Theme(id, THEME_ANIME_NAME, THEME_ANIME_DESCRIPTION, THEME_ANIME_THUMBNAIL);
    }
}

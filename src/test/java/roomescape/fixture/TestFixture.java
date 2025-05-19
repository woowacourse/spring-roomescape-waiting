package roomescape.fixture;

import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class TestFixture {

    public static ReservationTime createTimeWithoutId(int hour, int min) {
        return ReservationTime.createWithoutId(LocalTime.of(hour, min));
    }

    public static Theme createThemeWithoutId(String name, String description, String thumbnail) {
        return Theme.createWithoutId(name, description, thumbnail);
    }

    public static Member createMemberWithoutId(String name, String email, String password) {
        return Member.createWithoutId(name, email, password, Role.USER);
    }

    public static ReservationTime createTime(int hour, int min) {
        return ReservationTime.createWithId(1L, LocalTime.of(hour, min));
    }

    public static Theme createTheme(String name, String description, String thumbnail) {
        return Theme.createWithId(1L, name, description, thumbnail);
    }

    public static Member createMember(String name, String email, String password) {
        return Member.createWithId(1L, name, email, password, Role.USER);
    }

}

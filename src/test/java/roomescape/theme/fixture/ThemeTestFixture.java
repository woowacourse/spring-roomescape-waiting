package roomescape.theme.fixture;

import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ThemeTestFixture {

    public static ReservationTime createTime(int hour, int min) {
        return ReservationTime.createWithoutId(LocalTime.of(hour, min));
    }

    public static Theme createTheme(String name, String description, String thumbnail) {
        return Theme.createWithoutId(name, description, thumbnail);
    }

    public static Member createMember(String name, String email, String password) {
        return Member.createWithoutId(name, email, password, Role.USER);
    }

}

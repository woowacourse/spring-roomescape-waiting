package roomescape;

import java.time.LocalTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.domain.user.UserRole;

public class DomainFixtures {

    public static final User JUNK_USER = new User(
        1L,
        new UserName("라젤"),
        UserRole.USER,
        new Email("razel@email.com"),
        new Password("password")
    );
    public static final TimeSlot JUNK_TIME_SLOT = new TimeSlot(1L, LocalTime.of(10, 0));
    public static final Theme JUNK_THEME = new Theme(
            1L,
            "레벨2 탈출",
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
}

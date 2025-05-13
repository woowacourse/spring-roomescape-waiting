package roomescape;

import java.time.LocalTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.User;
import roomescape.domain.UserRole;

public class DomainFixtures {

    public static final User JUNK_USER = new User(1L, "포포", UserRole.USER, "popo@email.com", "password");
    public static final TimeSlot JUNK_TIME_SLOT = new TimeSlot(1L, LocalTime.of(10, 0));
    public static final Theme JUNK_THEME = new Theme(
        1L,
        "레벨2 탈출",
        "우테코 레벨2를 탈출하는 내용입니다.",
        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
}

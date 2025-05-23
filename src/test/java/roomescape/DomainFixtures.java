package roomescape;

import java.time.LocalTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRole;

public class DomainFixtures {

    public static final User JUNK_USER = User.ofExisting(1L, "라젤", UserRole.USER, "razel@email.com", "password");
    public static final TimeSlot JUNK_TIME_SLOT = TimeSlot.ofExisting(1L, LocalTime.of(10, 0));
    public static final Theme JUNK_THEME = Theme.ofExisting(
            1L,
            "레벨2 탈출",
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
}

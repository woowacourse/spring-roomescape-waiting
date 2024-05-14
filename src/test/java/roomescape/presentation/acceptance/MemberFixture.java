package roomescape.presentation.acceptance;

import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.PlayerName;
import roomescape.domain.Role;

public class MemberFixture {

    public static Member defaultValue() {
        return of("test", "admin@wooteco.com", "wootecoCrew6!", Role.ADMIN);
    }

    public static Member of(String name, String email, String password, Role role) {
        return new Member(new PlayerName(name), new Email(email), new Password(password), role);
    }
}

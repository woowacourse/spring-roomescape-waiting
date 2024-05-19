package roomescape.presentation.acceptance;

import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Password;
import roomescape.domain.member.PlayerName;
import roomescape.domain.member.Role;

public class MemberFixture {
    public static Member defaultValue() {
        return of("test", "admin@wooteco.com", "wootecoCrew6!", Role.ADMIN);
    }

    public static Member of(String name, String email, String password, Role role) {
        return new Member(new PlayerName(name), new Email(email), new Password(password), role);
    }
}

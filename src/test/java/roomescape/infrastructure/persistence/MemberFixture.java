package roomescape.infrastructure.persistence;

import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.PlayerName;
import roomescape.domain.Role;

public class MemberFixture {
    public static Member defaultValue() {
        return of("test", "test@email.com", "wootecoCrew6!");
    }

    public static Member of(String name, String email, String password) {
        return new Member(new PlayerName(name), new Email(email), new Password(password), Role.BASIC);
    }
}

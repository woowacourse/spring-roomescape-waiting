package roomescape.acceptance.fixture;

import roomescape.domain.user.Member;
import roomescape.domain.user.Role;

public class MemberFixture {
    public static Member getDomain() {
        return Member.from(
                "조이썬",
                "joyson5582@gmail.com",
                "password1234",
                Role.USER
        );
    }
}

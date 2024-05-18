package roomescape.acceptance.fixture;

import roomescape.domain.user.Member;

public class MemberFixture {
    public static Member getDomain() {
        return Member.fromMember(
                null,
                "조이썬",
                "joyson5582@gmail.com",
                "password1234"
        );
    }
}

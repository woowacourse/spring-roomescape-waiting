package roomescape.fixture;

import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Password;
import roomescape.domain.member.PlayerName;

public class MemberFixture {

    public static Member createMember(String name) {
        return new Member(
                new PlayerName(name),
                new Email("test@test.com"),
                new Password("12341234")
        );
    }
}

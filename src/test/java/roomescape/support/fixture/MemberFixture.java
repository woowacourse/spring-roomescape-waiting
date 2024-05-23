package roomescape.support.fixture;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;


public class MemberFixture {

    public static Member create() {
        return create("prin@email.com");
    }

    public static Member create(String email) {
        return new Member(email, "password", "프린", Role.USER);
    }
}

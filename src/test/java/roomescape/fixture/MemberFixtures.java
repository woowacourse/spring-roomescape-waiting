package roomescape.fixture;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public class MemberFixtures {

    private MemberFixtures() {
    }

    public static Member createAdminMember(String name, String email) {
        return new Member(null, name, email, "default", MemberRole.ADMIN);
    }
}

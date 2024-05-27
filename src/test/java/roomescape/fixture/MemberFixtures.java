package roomescape.fixture;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public class MemberFixtures {

    private MemberFixtures() {
    }

    public static Member createAdminMemberDaon(String email) {
        return new Member("daon", email, "1234", MemberRole.ADMIN);
    }

    public static Member createAdminMemberDaon(String email, String password) {
        return new Member("daon", email, password, MemberRole.ADMIN);
    }

    public static Member createMemberDaon(String email, String password, MemberRole memberRole) {
        return new Member("daon", email, password, memberRole);
    }
}

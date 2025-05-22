package roomescape.fixture;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class LoginMemberFixture {

    private LoginMemberFixture() {
    }

    public static Member getAdmin() {
        return new Member(1L, "어드민", "admin@gmail.com", "wooteco7", Role.ADMIN);
    }

    public static Member getUser() {
        return new Member(2L, "회원", "user@gmail.com", "wooteco7", Role.USER);
    }
}

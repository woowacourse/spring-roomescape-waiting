package roomescape.fixture;

import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;

public class LoginMemberFixture {

    private LoginMemberFixture() {
    }

    public static Member getAdmin() {
        return new Member(1L, "어드민", new Email("admin@gmail.com"), new Password("wooteco7"), Role.ADMIN);
    }

    public static Member getUser() {
        return new Member(2L, "회원", new Email("user@gmail.com"), new Password("wooteco7"), Role.USER);
    }
}

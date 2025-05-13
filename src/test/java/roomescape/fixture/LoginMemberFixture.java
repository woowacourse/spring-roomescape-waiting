package roomescape.fixture;

import roomescape.domain.LoginMember;

public class LoginMemberFixture {

    private LoginMemberFixture() {
    }

    public static LoginMember getAdmin() {
        return new LoginMember(1L, "어드민", "admin@gmail.com", "wooteco7", "ADMIN");
    }

    public static LoginMember getUser() {
        return new LoginMember(2L, "회원", "user@gmail.com", "wooteco7", "USER");
    }
}

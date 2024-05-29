package roomescape.fixture;

import roomescape.domain.LoginMember;
import roomescape.domain.Role;
import roomescape.entity.Member;

public class MemberFixture {
    public static final LoginMember DEFAULT_LOGIN_MEMBER = new LoginMember(1L, "name", Role.USER);
    public static final Member DEFAULT_MEMBER = new Member(DEFAULT_LOGIN_MEMBER, "email@email.com", "password");
}

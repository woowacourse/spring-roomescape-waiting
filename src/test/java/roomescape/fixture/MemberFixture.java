package roomescape.fixture;

import roomescape.domain.Member;
import roomescape.domain.Role;

public class MemberFixture {

    public static Member ADMIN_MEMBER =
            new Member(1L, "어드민", "testDB@email.com", "1234", Role.ADMIN);

    public static Member USER_MEMBER =
            new Member(2L, "사용자", "test2DB@email.com", "1234", Role.USER);
}

package roomescape.support.fixture;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public class MemberFixture {
    public static final Member ADMIN = email("admin@gmail.com");

    public static Member email(String email) {
        return new Member(email, "password", "프린", Role.USER);
    }
}

package roomescape.support.fixture;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;


public class MemberFixture {

    public static final Member ADMIN = new Member("admin@email.com", "password", "관리자", Role.ADMIN);

    public static final Member USER = new Member("user@email.com", "password", "유저", Role.USER);

    public static Member create() {
        return create("prin@email.com");
    }

    public static Member create(String email) {
        return new Member(email, "password", "프린", Role.USER);
    }
}

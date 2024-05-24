package roomescape.support.fixture;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;


public class MemberFixture {

    public static Member admin() {
        return new Member("admin@email.com", "password", "관리자", Role.ADMIN);
    }

    public static Member user() {
        return new Member("user@email.com", "password", "유저", Role.USER);
    }

    public static Member prin() {
        return new Member("prin@email.com", "password", "프린", Role.USER);
    }

    public static Member jamie() {
        return new Member("jamie@email.com", "password", "제이미", Role.USER);
    }

    public static Member create(String email) {
        return new Member(email, "password", "포비", Role.USER);
    }
}

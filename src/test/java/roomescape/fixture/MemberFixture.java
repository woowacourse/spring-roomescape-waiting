package roomescape.fixture;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class MemberFixture {
    public static Member getOne() {
        return new Member("name", Role.USER, "email@naver.com", "password");
    }

    public static Member getOneWithId(final Long id) {
        return new Member(id, "name", Role.USER, "email", "password");
    }

    public static Member getOne(final String email) {
        return new Member("name", Role.USER, email, "password");
    }

    public static Member getOne(final String email, final String password) {
        return new Member("name", Role.USER, email, password);
    }
}

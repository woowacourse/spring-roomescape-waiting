package roomescape.fixture;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class MemberFixture {
    public static Member getOne() {
        return new Member("name", Role.USER, "email@naver.com", "password");
    }

    public static Member getOneWithId(Long id) {
        return new Member(id, "name", Role.USER, "email@naver.com", "password");
    }

    public static Member getOne(String email) {
        return new Member("name", Role.USER, email, "password");
    }
}

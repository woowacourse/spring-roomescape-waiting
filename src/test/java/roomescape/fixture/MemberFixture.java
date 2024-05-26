package roomescape.fixture;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public class MemberFixture {

    public static Member createAdmin() {
        return new Member(1L, "관리자", "admin@a.com", "123a!", Role.ADMIN);
    }

    public static Member createUserWithIdTwo() {
        return new Member(2L, "사용자", "user@a.com", "123a!", Role.USER);
    }

    public static Member createUserWithIdThree() {
        return new Member(3L, "사용자2", "user2@a.com", "123a!", Role.USER);
    }
}

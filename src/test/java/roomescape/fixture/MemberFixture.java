package roomescape.fixture;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class MemberFixture {
    public static Member getMemberChoco() {
        return new Member(1L, "초코칩", "dev.chocochip@gmail.com", Role.USER);
    }

    public static Member getMemberClover() {
        return new Member(2L, "클로버", "dev.clover@gmail.com", Role.USER);
    }

    public static Member getMemberAdmin() {
        return new Member(3L, "관리자", "admin@roomescape.com", Role.ADMIN);
    }
}

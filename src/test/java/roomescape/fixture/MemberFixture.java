package roomescape.fixture;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class MemberFixture {
    public static Member getMemberChoco() {
        return new Member(null, "초코칩", "dev.chocochip@gmail.com", "1234", Role.USER);
    }

    public static Member getMemberClover() {
        return new Member(null, "클로버", "dev.clover@gmail.com", "qwer", Role.USER);
    }

    public static Member getMemberAdmin() {
        return new Member(null, "관리자", "admin@roomescape.com", "admin", Role.ADMIN);
    }
}

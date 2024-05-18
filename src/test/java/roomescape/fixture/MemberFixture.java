package roomescape.fixture;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class MemberFixture {
    public static Member getMemberChoco() {
        return new Member(1L, "초코칩", "dev.chocochip@gmail.com", "password", Role.USER);
    }

    public static Member getMemberClover() {
        return new Member( "클로버", "dev.clover@gmail.com","password", Role.USER);
    }

    public static Member getMemberAdmin() {
        return new Member(2L, "관리자", "admin@roomescape.com", "password", Role.ADMIN);
    }

    public static Member getMemberTacan() {
        return new Member( "타칸", "dev.tacan@gmail.com","1234", Role.USER);
    }
}

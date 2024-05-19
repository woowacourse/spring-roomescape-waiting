package roomescape.fixture;

import roomescape.domain.member.domain.Member;

import static roomescape.domain.member.domain.Role.ADMIN;
import static roomescape.domain.member.domain.Role.MEMBER;

public class MemberFixture {
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "123";
    private static final String MEMBER_EMAIL = "pokpo@gmail.com";
    private static final String MEMBER_PASSWORD = "123";

    public static final Member ADMIN_MEMBER = new Member(1L, "어드민", ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN);
    public static final Member MEMBER_MEMBER = new Member(1L, "폭포", MEMBER_EMAIL, MEMBER_PASSWORD, MEMBER);
}

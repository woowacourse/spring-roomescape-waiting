package roomescape.fixture;

import static roomescape.domain.member.domain.Role.ADMIN;
import static roomescape.domain.member.domain.Role.MEMBER;

import roomescape.domain.member.domain.Member;

public class MemberFixture {

    public static final String ADMIN_EMAIL = "admin@gmail.com";
    public static final String ADMIN_PASSWORD = "123456";
    public static final Member ADMIN_MEMBER = new Member(1L, "어드민", ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN);

    public static final String MEMBER_EMAIL = "dodo@gmail.com";
    public static final String MEMBER_PASSWORD = "123123";
    public static final Member MEMBER_MEMBER = new Member(1L, "도도", MEMBER_EMAIL, MEMBER_PASSWORD, MEMBER);
}

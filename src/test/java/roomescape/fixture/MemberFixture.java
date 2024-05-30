package roomescape.fixture;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public class MemberFixture {
    public static final Member MEMBER_ADMIN = new Member(1L, "관리자", "admin@abc.com", "1234", MemberRole.ADMIN);
    public static final Member MEMBER_BROWN = new Member(2L, "브라운", "brown@abc.com", "1234", MemberRole.USER);
    public static final Member MEMBER_BRI = new Member(3L, "브리", "bri@abc.com", "1234", MemberRole.USER);
    public static final Member MEMBER_DUCK = new Member(4L, "오리", "duck@abc.com", "1234", MemberRole.USER);
}

package roomescape.fixture.domain;

import roomescape.auth.domain.AuthRole;
import roomescape.member.domain.Member;

public class MemberFixture {

    public static final Member NOT_SAVED_MEMBER_1 = new Member("헤일러", "he@iler.com", "비밀번호", AuthRole.MEMBER);
    public static final Member NOT_SAVED_MEMBER_2 = new Member("머피", "mu@ffy.com", "비밀번호", AuthRole.MEMBER);
    public static final Member NOT_SAVED_MEMBER_3 = new Member("매트", "ma@t.com", "비밀번호", AuthRole.MEMBER);
}

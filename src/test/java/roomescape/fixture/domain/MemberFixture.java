package roomescape.fixture.domain;

import roomescape.auth.domain.AuthRole;
import roomescape.member.domain.Member;

public class MemberFixture {

    public static Member notSavedMember1() {
        return new Member("헤일러", "he@iler.com", "비밀번호", AuthRole.MEMBER);
    }

    public static Member notSavedMember2() {
        return new Member("머피", "mu@ffy.com", "비밀번호", AuthRole.MEMBER);
    }
}

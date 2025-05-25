package roomescape.fixture.domain;

import java.util.List;
import roomescape.auth.domain.AuthRole;
import roomescape.member.domain.Member;

public class MemberFixture {
    public static List<Member> notSavedMembers = List.of(

    );

    public static Member notSavedMember1() {
        return Member.of("헤일러", "he@iler.com", "비밀번호", AuthRole.MEMBER);
    }

    public static Member notSavedMember2() {
        return Member.of("머피", "mu@ffy.com", "비밀번호", AuthRole.MEMBER);
    }
}

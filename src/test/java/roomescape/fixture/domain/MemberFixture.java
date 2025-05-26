package roomescape.fixture.domain;

import java.util.List;
import roomescape.auth.domain.AuthRole;
import roomescape.member.domain.Member;

public class MemberFixture {

    private MemberFixture() {
    }

    public static List<Member> notSavedMembers(int exclusiveIdx) {
        final List<Member> NOT_SAVED_MEMBERS = List.of(
                Member.of("헤일러", "he@iler.com", "비밀번호", AuthRole.MEMBER),
                Member.of("머피", "mu@ffy.com", "비밀번호", AuthRole.MEMBER),
                Member.of("우가", "woo@ga.com", "비밀번호", AuthRole.MEMBER),
                Member.of("피케이", "p@k.com", "비밀번호", AuthRole.MEMBER),
                Member.of("매트", "ma@tt.com", "비밀번호", AuthRole.MEMBER),
                Member.of("파랑", "pa@rang.com", "비밀번호", AuthRole.MEMBER),
                Member.of("회원1", "member1@member1.com", "비밀번호", AuthRole.MEMBER),
                Member.of("회원2", "member2@member2.com", "비밀번호", AuthRole.MEMBER)
        );

        if (NOT_SAVED_MEMBERS.size() < exclusiveIdx) {
            throw new IllegalStateException("회원 픽스처의 개수는 최대 " + NOT_SAVED_MEMBERS.size() + "개만 가능합니다.");
        }

        return NOT_SAVED_MEMBERS.subList(0, exclusiveIdx);
    }

    public static Member notSavedMember1() {
        return Member.of("헤일러", "he@iler.com", "비밀번호", AuthRole.MEMBER);
    }

    public static Member notSavedMember2() {
        return Member.of("머피", "mu@ffy.com", "비밀번호", AuthRole.MEMBER);
    }

    public static Member notSavedMember3() {
        return Member.of("우가", "woo@ga.com", "비밀번호", AuthRole.MEMBER);
    }
}

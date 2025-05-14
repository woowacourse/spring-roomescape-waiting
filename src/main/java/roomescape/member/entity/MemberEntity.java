package roomescape.member.entity;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record MemberEntity(String name, String email, String password, String memberRole) {

    public Member toMember() {
        return Member.of(name, email, password, MemberRole.from(memberRole));
    }
}

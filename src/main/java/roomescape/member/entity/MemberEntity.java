package roomescape.member.entity;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record MemberEntity(Long id, String name, String email, String password, String memberRole) {

    public Member toMember() {
        return Member.of(id, name, email, password, MemberRole.from(memberRole));
    }
}

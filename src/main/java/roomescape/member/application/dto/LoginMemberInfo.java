package roomescape.member.application.dto;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record LoginMemberInfo(long id, String name, String email, MemberRole memberRole) {

    public LoginMemberInfo(final Member member) {
        this(member.id(), member.memberName().name(), member.email(), member.role());
    }

    public boolean isNotAdmin() {
        return memberRole != MemberRole.ADMIN;
    }
}

package roomescape.member.service.dto;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record LoginMemberInfo(long id, String name, String email, MemberRole memberRole) {

    public LoginMemberInfo(final Member member) {
        this(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }

    public boolean isNotAdmin() {
        return memberRole != MemberRole.ADMIN;
    }
}

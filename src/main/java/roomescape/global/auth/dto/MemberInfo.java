package roomescape.global.auth.dto;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record MemberInfo(Long id, MemberRole memberRole) {

    public static MemberInfo from(final Member member) {
        return new MemberInfo(member.getId(), member.getMemberRole());
    }
}

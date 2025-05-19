package roomescape.global.auth.dto;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record UserInfo(Long id, MemberRole memberRole) {

    public static UserInfo from(final Member member) {
        return new UserInfo(member.getId(), member.getMemberRole());
    }
}

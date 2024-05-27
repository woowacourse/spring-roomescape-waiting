package roomescape.dto;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public record TokenInfo(String payload, MemberRole memberRole) {

    public TokenInfo(Member member) {
        this(member.getEmail(), member.getRole());
    }

    public String getRole() {
        return memberRole.name();
    }
}

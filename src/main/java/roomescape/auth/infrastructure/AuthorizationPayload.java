package roomescape.auth.infrastructure;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record AuthorizationPayload(
    String name,
    MemberRole role
) {
    public static AuthorizationPayload fromMember(Member member) {
        return new AuthorizationPayload(
            member.getName(),
            member.getRole()
        );
    }
}

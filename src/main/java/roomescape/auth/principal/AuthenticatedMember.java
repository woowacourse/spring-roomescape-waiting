package roomescape.auth.principal;

import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;

public record AuthenticatedMember(
        Long id,
        String name,
        String email,
        MemberRole role
) {
    public static AuthenticatedMember from(final Member member) {
        return new AuthenticatedMember(
                member.getId(),
                member.getName().value(),
                member.getEmail().value(),
                member.getRole()
        );
    }
}

package roomescape.application.auth.dto;

import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;

public record TokenPayload(long memberId, String name, Role role) {

    public static TokenPayload from(MemberRole memberRole) {
        return new TokenPayload(memberRole.getMemberId(), memberRole.getMemberName(), memberRole.getRole());
    }

    public boolean hasRoleOf(Role requiredRole) {
        return role == requiredRole;
    }

    public String roleName() {
        return role.name();
    }
}

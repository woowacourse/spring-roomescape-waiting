package roomescape.application.auth.dto;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record TokenPayload(long memberId, String name, Role role) {

    public static TokenPayload from(Member member) {
        return new TokenPayload(member.getId(), member.getName(), member.getRole());
    }

    public boolean hasRoleOf(Role requiredRole) {
        return role == requiredRole;
    }

    public String roleName() {
        return role.name();
    }
}

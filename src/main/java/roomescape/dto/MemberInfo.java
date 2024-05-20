package roomescape.dto;

import roomescape.domain.Role;

public record MemberInfo(long id, String name, Role role) {
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
}

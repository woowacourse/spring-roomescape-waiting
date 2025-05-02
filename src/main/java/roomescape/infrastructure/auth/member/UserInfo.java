package roomescape.infrastructure.auth.member;

import roomescape.domain.member.Role;

public record UserInfo(Long id, String username, String name, Role role) {

    public boolean isAdmin() {
        return role.isAdmin();
    }
}

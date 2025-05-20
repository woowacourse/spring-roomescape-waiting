package roomescape.dto.business;

import roomescape.domain.Role;

public record AccessTokenContent(Long id, Role role, String name) {

    public boolean isAdminToken() {
        return role == Role.ADMIN;
    }
}

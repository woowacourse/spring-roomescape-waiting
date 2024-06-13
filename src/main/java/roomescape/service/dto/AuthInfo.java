package roomescape.service.dto;

import static roomescape.domain.Role.ADMIN;

import roomescape.domain.Role;

public record AuthInfo(Long id, String name, Role role) {

    public boolean isRole(Role role) {
        return this.role == role;
    }
}

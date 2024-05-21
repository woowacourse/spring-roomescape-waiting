package roomescape.dto.auth;

import roomescape.domain.member.Role;

public record LoginMember(
        Long id,
        String name,
        Role role
) {

    public boolean isNotAdmin() {
        return role != Role.ADMIN;
    }
}

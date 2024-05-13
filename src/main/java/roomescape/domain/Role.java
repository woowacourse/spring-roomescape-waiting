package roomescape.domain;

import roomescape.domain.exception.RoleNotFoundException;

public enum Role {

    ADMIN,
    USER;

    public static Role findByName(final String target) {
        if ("ADMIN".equals(target)) {
            return Role.ADMIN;
        }
        if ("USER".equals(target)) {
            return Role.USER;
        }
        throw new RoleNotFoundException("존재하지 않는 role입니다.");
    }
}

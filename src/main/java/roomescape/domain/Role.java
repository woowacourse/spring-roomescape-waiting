package roomescape.domain;

import java.util.Arrays;
import roomescape.exception.local.InvalidRoleException;

public enum Role {

    ROLE_MEMBER,
    ROLE_ADMIN,
    ;

    public static Role findByName(String name) {
        return Arrays.stream(Role.values())
                .filter(o -> o.name().equals(name))
                .findFirst()
                .orElseThrow(InvalidRoleException::new);
    }
}

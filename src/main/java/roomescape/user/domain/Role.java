package roomescape.user.domain;

import java.util.Arrays;
import roomescape.user.exception.InvalidRoleException;

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

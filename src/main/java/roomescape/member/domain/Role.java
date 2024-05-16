package roomescape.member.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.InternalException;

public enum Role {
    USER,
    ADMIN,
    ;

    public static Role of(String roleString) {
        for (Role role : Role.values()) {
            if (roleString.equalsIgnoreCase(role.name())) {
                return role;
            }
        }
        throw new InternalException(ErrorType.UNEXPECTED_SERVER_ERROR);
    }
}

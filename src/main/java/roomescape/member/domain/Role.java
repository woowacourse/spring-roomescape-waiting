package roomescape.member.domain;

import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;

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
        throw new BadRequestException(ErrorType.UNEXPECTED_SERVER_ERROR);
    }
}

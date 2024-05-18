package roomescape.member.domain;

import roomescape.exception.custom.InternalServerException;

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
        throw new InternalServerException("서버 관리자에게 문의하세요.");
    }
}

package roomescape.domain.member.domain;

import java.util.Arrays;
import roomescape.global.exception.EscapeApplicationException;

public enum Role {
    ADMIN("admin"), MEMBER("member");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role convertToRole(String value) {
        return Arrays.stream(values())
                .filter(role -> role.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new EscapeApplicationException("존재하지 않는 역할입니다."));
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public String getValue() {
        return value;
    }
}

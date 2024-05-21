package roomescape.domain;

import java.util.Arrays;

public enum Role {

    ADMIN,
    USER;

    public static Role of(String roleName) {
        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(roleName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("역할을 찾을 수 없습니다."));
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isNotAdmin() {
        return !isAdmin();
    }

}

package roomescape.auth;

import java.util.Arrays;

public enum Role {
    MEMBER,
    ADMIN,
    ;

    public static Role from(String roleData) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(roleData))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("일치하는 권한이 없습니다."));
    }

    public boolean isAdmin() {
        return this.name().equals("ADMIN");
    }
}

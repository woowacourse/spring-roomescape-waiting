package roomescape.member.domain;

import java.util.Arrays;

public enum Role {
    USER("user"),
    ADMIN("admin");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public static Role findRole(String findRole) {
        return Arrays.stream(Role.values())
                .filter(role -> role.role.equals(findRole))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("역할을 찾을 수 없습니다."));
    }

    public String getRole() {
        return role;
    }
}

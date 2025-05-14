package roomescape.model;

import java.util.Arrays;

public enum Role {
    ADMIN("ADMIN"),
    USER("USER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role fromValue(String role) {
        return Arrays.stream(Role.values())
                .filter(roleName -> roleName.name().equalsIgnoreCase(role))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("조건에 알맞는 Role 이 존재하지 않습니다."));
    }

    public String getValue() {
        return value;
    }
}

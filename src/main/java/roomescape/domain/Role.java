package roomescape.domain;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Role {

    ADMIN("ADMIN"),
    USER("USER");

    private final String value;

    Role(final String value) {
        this.value = value;
    }

    public static Role fromValue(final String role) {
        return Arrays.stream(Role.values())
                .filter(roleName -> roleName.name().equalsIgnoreCase(role))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("조건에 알맞는 Role 이 존재하지 않습니다."));
    }

    public static boolean isAdmin(final Role role) {
        return ADMIN.equals(role);
    }

}

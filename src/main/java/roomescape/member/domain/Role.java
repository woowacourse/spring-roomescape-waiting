package roomescape.member.domain;

import java.util.Arrays;

public enum Role {

    MEMBER, ADMIN, USER;

    public boolean isEqual(Role role) {
        return this.equals(role);
    }

    public static Role from(String value) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(value.toUpperCase()))
                .findFirst()
                .orElse(Role.USER);
    }
}

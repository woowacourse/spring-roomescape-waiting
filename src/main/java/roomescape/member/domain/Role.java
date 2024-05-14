package roomescape.member.domain;

import java.util.Arrays;

public enum Role {
    ADMIN,
    MEMBER;

    public static Role from(final String role) {
        return Arrays.stream(Role.values())
                .filter(element -> element.name().equals(role.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 역할입니다."));
    }
}

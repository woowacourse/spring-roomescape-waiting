package roomescape.member.domain;

import java.util.Arrays;

public enum Role {

    ADMIN, USER;

    public static Role of(final String name) {
        return Arrays.stream(Role.values())
                .filter(role -> role.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("권한을 찾을 수 없습니다."));
    }
}

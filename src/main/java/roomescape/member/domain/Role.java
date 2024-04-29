package roomescape.member.domain;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum Role {
    ADMIN("ADMIN"),
    MEMBER("MEMBER"),
    GUEST("GUEST");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role from(String value) {
        return Arrays.stream(Role.values())
                .filter(role -> role.value.equals(value))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(value + "에 해당하는 역할을 찾지 못했습니다"));
    }
}

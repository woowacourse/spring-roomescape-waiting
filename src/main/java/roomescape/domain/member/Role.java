package roomescape.domain.member;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Role {
    NORMAL("normal"),
    ADMIN("admin");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role of(String value) {
        return Arrays.stream(values())
                .filter(role -> role.getValue().equals(value))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    public boolean isAdmin() {
        return this == Role.ADMIN;
    }
}

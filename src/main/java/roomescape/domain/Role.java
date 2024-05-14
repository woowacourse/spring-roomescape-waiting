package roomescape.domain;

import java.util.Arrays;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;

public enum Role {
    ADMIN("role1"),
    USER("role2");

    private final String tokenValue;

    Role(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public static Role findByValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.tokenValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new RoomescapeException(ExceptionType.NOT_FOUND_ROLE));
    }

    public String getTokenValue() {
        return tokenValue;
    }
}

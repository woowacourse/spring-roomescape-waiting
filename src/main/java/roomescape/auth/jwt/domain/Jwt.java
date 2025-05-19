package roomescape.auth.jwt.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Jwt {

    private final String value;

    public static Jwt from(final String value) {
        return new Jwt(value);
    }
}

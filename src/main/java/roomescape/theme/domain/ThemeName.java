package roomescape.theme.domain;

import java.util.Objects;

public record ThemeName(String value) {
    private static final int MAX_LENGTH = 30;

    public ThemeName {
        Objects.requireNonNull(value);
        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("테마 이름은 1글자 이상 30글자 미만이어야 합니다.");
        }
    }
}

package roomescape.theme.domain;

import java.util.Objects;

public record ThemeDescription(String value) {
    private static final int MAX_LENGTH = 255;

    public ThemeDescription {
        Objects.requireNonNull(value);
        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("테마 설명은 1글자 이상 255글자 이하이어야 합니다.");
        }
    }
}

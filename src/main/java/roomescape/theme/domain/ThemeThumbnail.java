package roomescape.theme.domain;

import java.util.Objects;

public record ThemeThumbnail(String value) {
    private static final int MAX_LENGTH = 500;

    public ThemeThumbnail {
        Objects.requireNonNull(value);
        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("테마 썸네일은 1글자 이상 500글자 이하이어야 합니다.");
        }
    }
}

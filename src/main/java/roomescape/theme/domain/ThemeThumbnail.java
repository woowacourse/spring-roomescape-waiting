package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record ThemeThumbnail(@Column(length = 500, nullable = false) String thumbnail) {
    private static final int MAX_LENGTH = 500;

    public ThemeThumbnail {
        Objects.requireNonNull(thumbnail);
        if (thumbnail.isEmpty() || thumbnail.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("테마 썸네일은 1글자 이상 500글자 이하이어야 합니다.");
        }
    }
}

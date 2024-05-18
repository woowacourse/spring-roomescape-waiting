package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public record ThemeDescription(
        @Column(length = 255, nullable = false)
        String description) {

    private static final int MAX_LENGTH = 255;

    public ThemeDescription {
        Objects.requireNonNull(description);
        if (description.isEmpty() || description.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("테마 설명은 1글자 이상 255글자 이하이어야 합니다.");
        }
    }
}

package roomescape.domain.theme;

import java.util.Objects;

public record ThemeDescription(String description) {
    private static final int MAX_DESCRIPTION_LENGTH = 30;

    public ThemeDescription(final String description) {
        this.description = Objects.requireNonNull(description, "description은 null일 수 없습니다.");
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalStateException("description은 " + MAX_DESCRIPTION_LENGTH + "자 이내여야 합니다.");
        }
    }
}

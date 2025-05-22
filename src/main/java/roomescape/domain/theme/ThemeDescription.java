package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Embeddable
public record ThemeDescription(
        @Column(nullable = false)
        @Size(max = ThemeDescription.MAX_DESCRIPTION_LENGTH)
        String description
) {
    private static final int MAX_DESCRIPTION_LENGTH = 30;

    public ThemeDescription(final String description) {
        this.description = Objects.requireNonNull(description, "description은 null일 수 없습니다.");
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalStateException("description은 " + MAX_DESCRIPTION_LENGTH + "자 이내여야 합니다.");
        }
    }
}

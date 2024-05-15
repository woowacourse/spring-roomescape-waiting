package roomescape.reservation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ThemeDescription {

    private static final int MAXIMUM_ENABLE_NAME_LENGTH = 80;

    @Column(length = 500, nullable = false)
    private String description;

    protected ThemeDescription() {
    }

    public ThemeDescription(final String description) {
        validateValue((description));
        this.description = description;
    }

    private void validateValue(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("테마 설명은 1글자 이상 80글자 이하여야 합니다.");
        }

        final String stripedValue = value.strip();
        if (stripedValue.isEmpty() || stripedValue.length() > MAXIMUM_ENABLE_NAME_LENGTH) {
            throw new IllegalArgumentException("테마 설명은 1글자 이상 80글자 이하여야 합니다.");
        }
    }

    public String getDescription() {
        return description;
    }
}

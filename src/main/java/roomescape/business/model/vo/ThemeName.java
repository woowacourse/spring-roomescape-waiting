package roomescape.business.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.InvalidCreateArgumentException;

@Embeddable
public record ThemeName(
        @Column(name = "theme_name")
        String value
) {
    private static final int MAX_LENGTH = 20;

    public ThemeName {
        validateMaxLength(value);
    }

    private static void validateMaxLength(final String name) {
        if (name.length() > MAX_LENGTH) {
            throw new InvalidCreateArgumentException(ErrorCode.THEME_NAME_TOO_LONG, MAX_LENGTH);
        }
    }
}

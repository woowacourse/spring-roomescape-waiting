package roomescape.domain;

import lombok.Getter;
import roomescape.exception.ErrorCode;
import roomescape.exception.ThemeException;

@Getter
public class ThemeName {

    private final String name;

    public ThemeName(final String value) {
        validate(value);
        this.name = value;
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new ThemeException(ErrorCode.THEME_NAME_NULL_OR_BLANK);
        }
    }
}

package roomescape.domain;

import lombok.Getter;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

@Getter
public class ThemeName {

    private final String name;

    public ThemeName(final String value) {
        validate(value);
        this.name = value;
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.THEME_NAME_NULL_OR_BLANK);
        }
    }
}

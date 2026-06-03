package roomescape.domain;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

public record ThemeName(
        String name
) {

    public ThemeName {
        validate(name);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.THEME_NAME_NULL_OR_BLANK);
        }
    }
}

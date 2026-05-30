package roomescape.feature.theme.domain;

import org.springframework.util.StringUtils;
import roomescape.feature.theme.error.type.ThemeErrorType;
import roomescape.global.error.exception.GeneralException;

public record ThemeName(String value) {

    private static final int MAXIMUM_LENGTH = 255;

    public ThemeName {
        if (!StringUtils.hasText(value) || value.length() > MAXIMUM_LENGTH) {
            throw new GeneralException(ThemeErrorType.INVALID_NAME);
        }
    }
}

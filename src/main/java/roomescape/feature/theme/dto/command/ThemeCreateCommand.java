package roomescape.feature.theme.dto.command;

import roomescape.feature.theme.domain.ThemeDescription;
import roomescape.feature.theme.domain.ThemeImageUrl;
import roomescape.feature.theme.domain.ThemeName;
import roomescape.feature.theme.error.type.ThemeErrorType;
import roomescape.global.error.exception.GeneralException;

public record ThemeCreateCommand(
    ThemeName name,
    ThemeDescription description,
    ThemeImageUrl imageUrl
) {
    public ThemeCreateCommand {
        if (name == null) {
            throw new GeneralException(ThemeErrorType.INVALID_NAME);
        }
        if (description == null) {
            throw new GeneralException(ThemeErrorType.INVALID_DESCRIPTION);
        }
        if (imageUrl == null) {
            throw new GeneralException(ThemeErrorType.INVALID_IMAGE_URL);
        }
    }
}

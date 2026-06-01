package roomescape.theme.application.dto;

import static roomescape.global.validation.ValidationUtils.requireNotBlank;

import roomescape.global.exception.ThemeErrorCode;

public record ThemeCreateCommand(
        String name,
        String description,
        String thumbnail
) {
    public ThemeCreateCommand {
        requireNotBlank(name, ThemeErrorCode.THEME_NAME_REQUIRED);
        requireNotBlank(description, ThemeErrorCode.THEME_DESCRIPTION_REQUIRED);
        requireNotBlank(thumbnail, ThemeErrorCode.THEME_THUMBNAIL_REQUIRED);
    }
}

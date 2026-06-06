package roomescape.controller.dto.response;

import roomescape.domain.Theme;
import roomescape.service.dto.ThemeInfo;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }

    public static ThemeResponse from(ThemeInfo themeInfo) {
        return new ThemeResponse(
                themeInfo.id(),
                themeInfo.name(),
                themeInfo.description(),
                themeInfo.thumbnailUrl()
        );
    }
}

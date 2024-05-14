package roomescape.reservation.controller.dto.response;

import roomescape.reservation.domain.Theme;

public record ThemeResponse(
        long themeId,
        String name,
        String description,
        String thumbnail
) {
    public static ThemeResponse from(final Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getThemeNameValue(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}

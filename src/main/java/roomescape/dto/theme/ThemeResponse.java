package roomescape.dto.theme;

import roomescape.domain.theme.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getThemeName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}

package roomescape.web.dto.response;

import roomescape.domain.theme.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String thumbnailUrl,
        String description

) {
    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getThemeName(), theme.getThumbnailUrl(), theme.getDescription());
    }
}

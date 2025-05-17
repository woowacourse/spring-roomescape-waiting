package roomescape.theme.dto.response;

import roomescape.theme.domain.Theme;

public record ThemeResponse(Long id, String name, String description, String thumbnail) {
    public static ThemeResponse from(Theme findTheme) {
        return new ThemeResponse(findTheme.getId(), findTheme.getName(), findTheme.getDescription(),
                findTheme.getThumbnail());
    }
}

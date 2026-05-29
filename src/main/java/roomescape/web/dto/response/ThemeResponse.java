package roomescape.web.dto.response;

import roomescape.domain.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String thumbnailUrl,
        String description

) {
    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.id(), theme.name(), theme.thumbnailUrl(), theme.description());
    }
}

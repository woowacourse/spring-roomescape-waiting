package roomescape.theme.presentation.dto.response;

import roomescape.theme.domain.Theme;

public record ThemeWebResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {
    public static ThemeWebResponse from(final Theme theme) {
        return new ThemeWebResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}

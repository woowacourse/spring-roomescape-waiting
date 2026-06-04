package roomescape.dto.response;

import roomescape.domain.Theme;

public record ThemeInfo(
        Long id,
        String name,
        String thumbnailUrl,
        String description
) {
    public static ThemeInfo from(Theme theme) {
        return new ThemeInfo(theme.id(), theme.name(), theme.thumbnailUrl(), theme.description());
    }
}
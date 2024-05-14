package roomescape.theme.dto;

import roomescape.theme.domain.Theme;

public record ThemeRequest(
        String name,
        String description,
        String thumbnail
) {
    public Theme toTheme() {
        return Theme.saveThemeOf(name, description, thumbnail);
    }
}

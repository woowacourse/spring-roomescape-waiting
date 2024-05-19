package roomescape.theme.dto;

import roomescape.global.domain.Name;
import roomescape.theme.domain.Theme;

public record ThemeRequest(
        String name,
        String description,
        String thumbnail
) {
    public Theme toTheme() {
        return new Theme(new Name(name), description, thumbnail);
    }
}

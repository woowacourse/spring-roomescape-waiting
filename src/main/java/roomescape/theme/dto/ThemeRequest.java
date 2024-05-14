package roomescape.theme.dto;

import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

public record ThemeRequest(String name, String description, String thumbnail) {

    public Theme toTheme() {
        return new Theme(null, new Name(name), description, thumbnail);
    }
}

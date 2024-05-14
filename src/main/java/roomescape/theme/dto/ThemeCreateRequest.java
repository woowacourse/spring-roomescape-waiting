package roomescape.theme.dto;

import roomescape.theme.domain.Theme;

public record ThemeCreateRequest(String name, String description, String thumbnail) {
    public Theme createTheme() {
        return new Theme(name, description, thumbnail);
    }
}

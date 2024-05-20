package roomescape.dto;

import roomescape.entity.Theme;

public record ThemeRequest(String name, String description, String thumbnail) {
    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
